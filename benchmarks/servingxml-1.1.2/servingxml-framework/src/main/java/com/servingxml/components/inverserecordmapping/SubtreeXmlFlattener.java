/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package com.servingxml.components.inverserecordmapping;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.app.Flow;
import com.servingxml.util.Stack;
import com.servingxml.components.saxsource.SaxEventBuffer;
import com.servingxml.components.saxsource.SaxEventBufferBuilder;
import com.servingxml.components.saxsource.SaxEventBufferSource;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.app.ParameterDescriptor;

import java.io.StringWriter;
import javax.xml.transform.URIResolver; 
import javax.xml.transform.Source; 
import javax.xml.transform.sax.SAXSource; 
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;


/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeXmlFlattener implements ShredXml {
  private final ParameterDescriptor[] parameterDescriptors;
  private final RestrictedMatchPattern expr;
  private final ShredXml flattener;
  private SaxEventBufferBuilder bufferBuilder;
  private Stack<SaxPath> elementStack = new Stack<SaxPath>();
  private boolean matched = false;
  private Flow matchedFlow;
  private int depth = 0;

  public SubtreeXmlFlattener(ParameterDescriptor[] parameterDescriptors,
    RestrictedMatchPattern expr, ShredXml flattener) {
    this.expr = expr;
    this.flattener = flattener;
    this.parameterDescriptors = parameterDescriptors;
  }

  public boolean isMatched() {
    return matched;
  }

  public void matchPath(ServiceContext context, Flow flow, SaxPath path) 
  throws SAXException {
    //System.out.println(getClass().getName()+".matchPath depth=" +depth + ", qname=" + path.getQname());

    if (!matched && expr.match(path,flow.getParameters())) {
      //System.out.println(getClass().getName()+".matchPath Expr matched");
      matched = true;
    }
  }

  public void startElement(ServiceContext context, Flow flow, SaxPath path,
    RecordWriter recordWriter) 
  throws SAXException {
    //System.out.println(getClass().getName()+".startElement depth=" +depth + ", qname=" + path.getQname());

    try {
      String namespaceUri = path.getNamespaceUri();
      String localName = path.getLocalName();
      String qname = path.getQname();
      //System.out.println(getClass().getName()+".startElement qname="+qname);

      Attributes atts = path.getAttributes();

      if (matched) {
        if (elementStack.empty()) {
          //System.out.println(getClass().getName()+".startElement matched first - depth="+depth);
          bufferBuilder = new SaxEventBufferBuilder();
          bufferBuilder.startDocument();
          bufferBuilder.startElement(namespaceUri,localName,qname,atts);
          SaxPath localPath = new SaxPath(context.getNameTable(),namespaceUri,localName,qname,atts);
          elementStack.push(localPath);
          matchedFlow = flow.augmentParameters(context,parameterDescriptors);
        } else {
          //System.out.println(getClass().getName()+".startElement matched subsequent - depth="+depth);
          SaxPath parent = elementStack.peek();
          SaxPath localPath = new SaxPath(namespaceUri,localName,qname,atts,parent);
          elementStack.push(localPath);
          flattener.matchPath(context,matchedFlow,localPath);
          if (!flattener.isMatched()) {
            //System.out.println(getClass().getName()+".startElement child not matched - depth="+depth);
            if (bufferBuilder != null) {
              bufferBuilder.startElement(namespaceUri,localName,qname,atts);
            }
          } else {
            //System.out.println(getClass().getName()+".startElement child matched - depth="+depth);
            if (bufferBuilder != null) {
              //System.out.println("Stack isn't empty...");
              for (int i = 0; i < elementStack.size(); ++i) {
                SaxPath path2 = elementStack.peek(i);
                bufferBuilder.endElement(path2.getNamespaceUri(),path2.getLocalName(),path2.getQname());
              }
              bufferBuilder.endDocument();
              SaxEventBuffer buffer = bufferBuilder.getBuffer();
              SaxSource saxSource = new SaxEventBufferSource(buffer,"", context.getTransformerFactory());
              //System.out.println("CHECK 1");
/*
              try {
                Source source = new SAXSource(saxSource.createXmlReader(),new InputSource(saxSource.getSystemId()));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                StringWriter writer = new StringWriter();
                Transformer transformer = transformerFactory.newTransformer();
                Result result = new StreamResult(writer);
                transformer.transform(source,result);
                //System.out.println (getClass().getName()+"startElement MATCH Subtree=" + writer.toString());
              } catch (Exception e) {
                //System.out.println(e.getMessage());
              }
*/              
              matchedFlow = matchedFlow.replaceDefaultSaxSource(context, saxSource);
              matchedFlow = matchedFlow.augmentParameters(context,parameterDescriptors);
              //System.out.println("Next: mapRecord "+depth);
              flattener.mapRecord(context, matchedFlow, recordWriter);
              bufferBuilder = null;

            }
            //System.out.println(getClass().getName()+".startElement Calling startElement on child "+depth);
            flattener.startElement(context, matchedFlow, localPath, recordWriter);
          }
          //System.out.println("Child flattener matched? "+flattener.isMatched());
        }
      }
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
    ++depth;
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (matched) {
      if (bufferBuilder != null) {
        bufferBuilder.characters(ch,start,length);
      }
      flattener.characters(ch, start, length);
    }
  }

  public void endElement(ServiceContext context, Flow flow2,
    String namespaceUri, String localName, String qname, RecordWriter recordWriter)
  throws SAXException {
    if (matched) {
      --depth;
      //System.out.println(getClass().getName()+".endElement qname="+qname);
      try {

        if (!elementStack.empty()) {
          //System.out.println(getClass().getName()+".startElement Calling endElement on child "+depth);
          flattener.endElement(context, matchedFlow,
            namespaceUri, localName, qname, recordWriter);
          elementStack.pop();
          if (elementStack.empty()) {
            matched = false;
          }
        }

        if (bufferBuilder != null) {
          bufferBuilder.endElement(namespaceUri,localName,qname);
          //System.out.println("POP STACK 2 " + depth);
          if (elementStack.empty()) {
            bufferBuilder.endDocument();
            SaxEventBuffer buffer = bufferBuilder.getBuffer();
            SaxSource saxSource = new SaxEventBufferSource(buffer,"", context.getTransformerFactory());
/*
            try {
              Source source = new SAXSource(saxSource.createXmlReader(),new InputSource(saxSource.getSystemId()));
              TransformerFactory transformerFactory = TransformerFactory.newInstance();
              //System.out.println ("XsltExpirable.cons check 15");
              StringWriter writer = new StringWriter();
              Transformer transformer = transformerFactory.newTransformer();
              Result result = new StreamResult(writer);
              //System.out.println ("XsltExpirable.cons check 20");
              transformer.transform(source,result);
              //System.out.println ("XsltExpirable.cons check 25");
              //System.out.println ("Subtree=" + writer.toString());
            } catch (Exception e) {
            }
*/            
            //for (int i = 0; i < parameterDescriptors.length; ++i) {
              //if (i > 0) {
                //System.out.print(",");
              //}
              //System.out.print(parameterDescriptors[i].getName());
            //}
            //if (parameterDescriptors.length > 0) {
              //System.out.println();
            //}

            //System.out.println(getClass().getName()+".endElement replace SAX source/augment parameters");
            Flow newFlow = matchedFlow.replaceDefaultSaxSource(context, saxSource);
            newFlow = newFlow.augmentParameters(context,parameterDescriptors);
            //System.out.println("Next: mapRecord "+depth);
            //flattener.endElement(context, newFlow,
            //  namespaceUri, localName, qname, recordWriter);
            flattener.mapRecord(context, newFlow, recordWriter);
            bufferBuilder = null;
          }
        }

      } catch (ServingXmlException e) {
        throw new SAXException(e.getMessage(),e);
      }
    }
  }

  public final void mapRecord(ServiceContext context, Flow flow, RecordWriter recordWriter)
  throws SAXException {
  }
}


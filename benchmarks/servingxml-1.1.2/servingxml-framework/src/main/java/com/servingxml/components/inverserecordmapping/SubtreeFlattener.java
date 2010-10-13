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

import java.io.StringWriter;
import org.xml.sax.SAXException;

import org.xml.sax.InputSource;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import com.servingxml.expr.saxpath.SaxPath;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.util.record.RecordContentHandler;

/**
 * A command for mapping a field in a flat file to an element or attribute
 * in an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeFlattener implements ShredXml {
  private final String matchExpr;
  private final Name recordTypeName;
  private final Templates templates;
  private final Name[] parameterNames;

  public SubtreeFlattener(String matchExpr, Name recordTypeName, 
    Templates templates, Name[] parameterNames) {
    this.matchExpr = matchExpr;
    this.recordTypeName = recordTypeName;
    this.templates = templates;
    this.parameterNames = parameterNames;
  }

  public boolean isMatched() {
    return false;
  }

  public void matchPath(ServiceContext context, Flow flow, SaxPath path) 
  throws SAXException {
  }

  public final void startElement(final ServiceContext context, Flow flow, SaxPath path,
    RecordWriter recordWriter) 
  throws SAXException {
  }

  public final void characters(char ch[], int start, int length) 
  throws SAXException {
  }

  public final void endElement(ServiceContext context, Flow flow,
    String namespaceUri, String localName, String qname, RecordWriter recordWriter)
  throws SAXException {
  }

  public final void mapRecord(final ServiceContext context, final Flow flow, 
    final RecordWriter recordWriter)
  throws SAXException {
    //System.out.println(getClass().getName()+".mapRecord enter");

    try {
      SaxSource saxSource = flow.getDefaultSaxSource();
/*
      try {
        Source source = new SAXSource(saxSource.createXmlReader(),new InputSource(saxSource.getSystemId()));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
          StringWriter writer = new StringWriter();
          Transformer transformer = transformerFactory.newTransformer();
          Result result = new StreamResult(writer);
          transformer.transform(source,result);
       //System.out.println ("Subtree=" + writer.toString());
      } catch (Exception e) {
      }
*/
      Source docSource = new SAXSource(saxSource.createXmlReader(),
                           new InputSource(saxSource.getSystemId()));
/*
      try {
        Source source = new SAXSource(saxSource.createXmlReader(),
                             new InputSource(saxSource.getSystemId()));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
          StringWriter writer = new StringWriter();
          Transformer transformer = transformerFactory.newTransformer();
          Result result = new StreamResult(writer);
          transformer.transform(source,result);
       //System.out.println ("Document=" + writer.toString());
      } catch (Exception e) {
      }
*/      
      Transformer transformer = templates.newTransformer();

      Record parameters = flow.getParameters();
      //System.out.println(getClass().getName()+".mapRecord fieldCount=" + parameters.fieldCount());
      //for (int i = 0; i < parameters.fieldCount(); ++i) {
        //System.out.println(getClass().getName()+".mapRecord flow param=" + parameters.getValue(i).getString());
      //}

      for (int i = 0; i < parameterNames.length; ++i) {
        Name parameterName = parameterNames[i];
        String value = parameters.getString(parameterName);
        //System.out.println(getClass().getName()+".mapRecord parameter=" + parameterName);
        if (value != null) {
          transformer.setParameter(parameterName.toString(),value);
          //System.out.println(getClass().getName()+".mapRecord value=" + value);
        }
      }

      RecordReceiver recordReceiver = new RecordReceiver() {
        public void receiveRecord(Record record) {
          Flow newFlow = flow.replaceRecord(context, record);
          recordWriter.writeRecord(context, newFlow);
         //System.out.println(getClass().getName()+".receiveRecord record=");
         //System.out.println(record.toXmlString(context));
        }
      };

      RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
      Result result = new SAXResult(receiver);
      transformer.transform(docSource,result);

    } catch (TransformerException te) {
      throw new SAXException(te.getMessage(),te);
    } catch (ServingXmlException te) {
      throw new SAXException(te.getMessage(),te);
    }
    //System.out.println(getClass().getName()+".mapRecord leave");
  }
}

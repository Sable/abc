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

package com.servingxml.extensions.saxon.xquery;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.content.AbstractContent;
import com.servingxml.components.content.Content;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMap.PrefixMapping;
import com.servingxml.util.ServingXmlException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.om.ValueRepresentation;

/**
 * A <code>XQueryContent</code> instance may be used to obtain objects that
 * supply XML content as an input stream.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XQueryContent extends AbstractContent implements Content {     

  private final PrefixMap prefixMap;
  private final StreamSourceFactory streamSourceFactory;
  private final Content contextDocument;
  private final String baseUri;

  public XQueryContent(String baseUri, PrefixMap prefixMap,
                                StreamSourceFactory streamSourceFactory,
                                Content contextDocument,
                                OutputPropertyFactory[] defaultOutputProperties) {
    super(defaultOutputProperties);

    this.baseUri = baseUri;
    this.prefixMap = prefixMap;
    this.streamSourceFactory = streamSourceFactory;
    this.contextDocument = contextDocument;
  }

  public String createString(ServiceContext context, Flow flow) {
    InputStream is = null;
    StreamSource streamSource = streamSourceFactory.createStreamSource(context,flow);
    try {
      is = streamSource.openStream();
      Processor proc = new Processor(false);
      XQueryCompiler comp = proc.newXQueryCompiler();
      PrefixMap.PrefixMapping[] prefixMappings = prefixMap.getLocalPrefixDeclarations();
      for (int i = 0; i < prefixMappings.length; ++i) {
        comp.declareNamespace(prefixMappings[i].getPrefix(), prefixMappings[i].getNamespaceUri());
      }
      if (baseUri != null && baseUri.length() != 0) {
        URI uri = new URI(baseUri);
        comp.setBaseURI(uri);
      }
      XQueryExecutable exp = comp.compile(is);
      XQueryEvaluator eval = exp.load();
      XdmValue value = eval.evaluate();
      ValueRepresentation valueRep = value.getUnderlyingValue();
      String s = valueRep.getStringValue();
      return s;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (streamSource != null && is != null) {
        try {
          streamSource.closeStream(is);
        } catch (IOException ee) {
        }
      }
    }
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".createSaxSource enter");
    InputStream is = null;
    StreamSource streamSource = streamSourceFactory.createStreamSource(context,flow);
    try {
      is = streamSource.openStream();
      Processor proc = new Processor(false);
      XQueryCompiler comp = proc.newXQueryCompiler();
      PrefixMap.PrefixMapping[] prefixMappings = prefixMap.getLocalPrefixDeclarations();
      for (int i = 0; i < prefixMappings.length; ++i) {
        comp.declareNamespace(prefixMappings[i].getPrefix(), prefixMappings[i].getNamespaceUri());
      }
      if (baseUri != null && baseUri.length() != 0) {
        URI uri = new URI(baseUri);
        comp.setBaseURI(uri);
      }
      XQueryExecutable exp = comp.compile(is);

      //String base = comp.getBaseURI() == null ? null : comp.getBaseURI().toString();
      SaxSource source = new XQuerySaxSource(prefixMap, context, flow, baseUri, contextDocument, exp);
      return source;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (streamSource != null && is != null) {
        try {
          streamSource.closeStream(is);
        } catch (IOException ee) {
        }
      }
    }
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = new XmlPipeline(/*defaultOutputProperties*/);
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
    return pipeline;
  }
}


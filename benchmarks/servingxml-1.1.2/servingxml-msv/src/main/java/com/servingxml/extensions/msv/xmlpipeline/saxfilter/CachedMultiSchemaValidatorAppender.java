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

package com.servingxml.extensions.msv.xmlpipeline.saxfilter;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.InputSource;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.content.XmlValidatorAppender;
import com.servingxml.components.saxsource.XmlSourceFactory;
import com.servingxml.extensions.msv.recordio.MsvHelper;
import com.servingxml.io.cache.Cache;
import com.servingxml.io.cache.CacheEntry;
import com.servingxml.io.cache.RevalidationType;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.util.ServingXmlException;
import com.sun.msv.verifier.jarv.TheFactoryImpl;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;

public class CachedMultiSchemaValidatorAppender extends AbstractXmlFilterAppender 
implements XmlValidatorAppender {

  private final VerifierFactory factory = new TheFactoryImpl();
  private final XmlSourceFactory xmlSourceFactory;
  private final RevalidationType revalidationType;
  private final Cache cache;
  private final ErrorHandler errorHandler;

  public CachedMultiSchemaValidatorAppender(XmlSourceFactory xmlSourceFactory,
                                   Cache cache, RevalidationType revalidationType,
                                    ErrorHandler errorHandler) {
    this.xmlSourceFactory = xmlSourceFactory;
    this.cache = cache;
    this.revalidationType = revalidationType;
    this.errorHandler = errorHandler;
  }

  public Schema getSchema(ServiceContext context, Flow flow) {
    StreamSource streamSource = null;
    InputStream is = null;
    try {
      streamSource = xmlSourceFactory.createStreamSource(context, flow);
      is = streamSource.openStream();
      InputSource inputSource = new InputSource(is);
      inputSource.setSystemId(streamSource.getSystemId());
      Schema schema = factory.compileSchema(inputSource);
      return schema;
    } catch (org.iso_relax.verifier.VerifierConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (org.xml.sax.SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (java.io.IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      try {
        if (streamSource != null) {
          streamSource.closeStream(is);
        }
      } catch (Exception t) {
      }
    }
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                               XmlFilterChain pipeline) {
    XMLFilter filter = createXmlFilter(context, flow);
    pipeline.addXmlFilter(filter);
  }

  public XMLFilter createXmlFilter(ServiceContext context, Flow flow) {
    Schema schema = getSchema(context, flow);
    return new MsvXmlFilter(schema, errorHandler);
  }

  public boolean validate(ServiceContext context, Flow flow, List<String> failures) {
    boolean valid = false;
    try {
      SaxSource saxSource = flow.getDefaultSaxSource();
      XMLReader xmlReader = saxSource.createXmlReader();
      XMLFilter schemaFilter = createXmlFilter(context,flow);
      schemaFilter.setParent(xmlReader);
      schemaFilter.parse("");
      valid = true;
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (Exception e) {
      String message = MsvHelper.makeMessage(e);
      failures.add(message);
    }

    return valid;
  }
}

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

import java.net.URL;

import org.xml.sax.ErrorHandler;

import com.servingxml.components.cache.ExpiryOptions;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.components.streamsource.url.UrlSourceFactory;
import com.servingxml.components.common.SimpleUrlEvaluator;
import com.servingxml.io.cache.Cache;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.saxsource.XmlSourceFactory;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.components.saxsource.StreamSourceXmlSourceFactory;
import com.servingxml.components.saxsource.SaxSourceXmlSourceFactory;
import com.servingxml.extensions.msv.CustomMsvErrorHandler;
import com.servingxml.extensions.msv.DefaultMsvErrorHandler;
import com.servingxml.components.content.XmlValidatorAppender;
import com.servingxml.components.content.XmlValidatorAppenderPrefilter;

public class MultiSchemaValidatorAppenderAssembler {

  private static final String UNBOUNDED = "unbounded";

  private ExpiryOptions expiryOptions = ExpiryOptions.CACHING_SYNCH;
  private Cache cache = null;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XmlSourceFactory xmlSourceFactory = null;
  private String message = null;

  public void setMessage(String message) {
    this.message = message;
  }

  public void setSchema(URL schemaUrl) {
    this.xmlSourceFactory = new StreamSourceXmlSourceFactory(new UrlSourceFactory(new SimpleUrlEvaluator(schemaUrl)));
  }

  public void injectComponent(Cache cache) {
    this.cache = cache;
  }

  public void injectComponent(ExpiryOptions expiryOptions) {
    this.expiryOptions = expiryOptions;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(StreamSourceFactory streamSourceFactory) {
    this.xmlSourceFactory = new StreamSourceXmlSourceFactory(streamSourceFactory);
  }

  public void injectComponent(SaxSourceFactory saxSourceFactory) {
    this.xmlSourceFactory = new SaxSourceXmlSourceFactory(saxSourceFactory);
  }

  public XmlValidatorAppender assemble(ConfigurationContext context) {

    if (xmlSourceFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:saxSource");
      throw new ServingXmlException(message);
    }

    ErrorHandler errorHandler;
    if (message != null) {
      errorHandler = new CustomMsvErrorHandler(message);
    } else {
      errorHandler = new DefaultMsvErrorHandler();
    }

    XmlValidatorAppender xmlFilterAppender; 
    //  revisit
    if (cache == null) {
      xmlFilterAppender = new MultiSchemaValidatorAppender(xmlSourceFactory, errorHandler);
    } else {
      xmlFilterAppender = new CachedMultiSchemaValidatorAppender(xmlSourceFactory,
                                                    cache, expiryOptions.getRevalidationType(), errorHandler);
    }
    if (parameterDescriptors.length > 0) {
      xmlFilterAppender = new XmlValidatorAppenderPrefilter(xmlFilterAppender,parameterDescriptors);
    }
    return xmlFilterAppender;
  }
}


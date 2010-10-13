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

package com.servingxml.components.xslt;

import com.servingxml.io.cache.Cache;
import com.servingxml.components.cache.ExpiryOptions;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.components.saxsource.SaxSourceFactoryAdaptor;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.components.content.Content;
import com.servingxml.components.saxfilter.XmlFilterAppenderPrefilter;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.common.UrlEvaluator;
import com.servingxml.components.common.SimpleUrlEvaluator;
import com.servingxml.components.parameter.WithParameters;
import com.servingxml.components.parameter.CompatabilityWithParameters;
import com.servingxml.app.Environment;

/**
 * The <code>XsltFilterFactoryAssembler</code> implements an assembler for
 * assembling <code>XsltFilterFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XsltFilterFactoryAssembler {
  private ExpiryOptions expiryOptions = ExpiryOptions.CACHING_SYNCH;
  private Cache cache = null;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name styleName = Name.EMPTY;
  private SaxSourceFactory saxSourceFactory = null;
  private String baseUri = "";
  private WithParameters withParameters;

  public void setDocumentBase(String baseUri) {
    this.baseUri = baseUri;
  }

  public void setBaseURI(String baseUri) {
    this.baseUri = baseUri;
  }

  public void injectComponent(StreamSourceFactory streamSourceFactory) {
    this.saxSourceFactory = new SaxSourceFactoryAdaptor(streamSourceFactory);
  }

  public void injectComponent(SaxSourceFactory saxSourceFactory) {
    this.saxSourceFactory = saxSourceFactory;
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

  public void injectComponent(WithParameters withParameters) {
    this.withParameters = withParameters;
  }
                                                                  
  public Content assemble(ConfigurationContext context) {
    Environment env = new Environment(parameterDescriptors,context.getQnameContext());

    if (saxSourceFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:streamSource or sx:saxSource or literal");
      throw new ServingXmlException(message);
    }

    try {
      //  Initialize context
      UrlEvaluator docBaseResolver = null;

      if (baseUri.length() > 0) {
        docBaseResolver = UrlEvaluator.parse(context.getQnameContext(), baseUri);
      }
      if (withParameters == null) {
        withParameters = new CompatabilityWithParameters(parameterDescriptors);
      }

      Content filterFactory = null;
      if (cache == null) {
        filterFactory = new XsltFilterFactory(env, saxSourceFactory, docBaseResolver,
                                               withParameters);
      } else {
        filterFactory = new CachedXsltFilterFactory(env, saxSourceFactory, docBaseResolver,
                                                     withParameters, 
                                                     cache, expiryOptions.getRevalidationType());
      }
      return filterFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}

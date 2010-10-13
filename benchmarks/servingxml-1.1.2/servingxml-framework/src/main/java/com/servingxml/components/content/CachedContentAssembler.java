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

package com.servingxml.components.content;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.cache.ExpiryOptions;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.io.cache.Cache;

/**
 * The <code>CachedContentAssembler</code> implements an assembler for
 * cached content.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CachedContentAssembler {
  private ExpiryOptions expirySettings = ExpiryOptions.CACHING_SYNCH;
  private Content contentFactory;
  private XsltConfiguration xsltConfiguration;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Cache cache = null;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Cache cache) {
    this.cache = cache;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(Content contentFactory) {
    this.contentFactory = contentFactory;
  }
  
  public void injectComponent(ExpiryOptions expirySettings) {
    this.expirySettings = expirySettings;
  }
                                         
  public Content assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }
    
    if (contentFactory == null) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:content");
      throw new ServingXmlException(msg);
    }

    if (cache != null) {
      contentFactory = new CachedContent(contentFactory, cache,
        expirySettings.getRevalidationType(), xsltConfiguration.getOutputPropertyFactories());
    }
    if (parameterDescriptors.length > 0) {
      contentFactory = new ContentPrefilter(contentFactory,parameterDescriptors);
    }
    return contentFactory;
  }
}

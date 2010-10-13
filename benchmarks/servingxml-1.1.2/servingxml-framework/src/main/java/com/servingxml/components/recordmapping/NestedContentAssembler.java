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

package com.servingxml.components.recordmapping;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.content.Content;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>NestedContentAssembler</code> implements an assembler for
 * assembling <code>MapXmlFactory</code> objects.
 *
 *                                               
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class NestedContentAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Content[] contentFactories = Content.EMPTY_ARRAY;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Content[] contentFactories) {
    this.contentFactories = contentFactories;
  }
                                                       
  public MapXmlFactory assemble(ConfigurationContext context) {

    if (contentFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_SPECIALIZATION_REQUIRED,
                                                                 context.getElement().getTagName(), "sx:content", "sx:recordContent");
      throw new ServingXmlException(message);
    }
    MapXmlFactory tailFactory = new MapXmlFactoryAdaptor(contentFactories);
    
    MapXmlFactory recordMapFactory = new NestedContent(tailFactory);
    if (parameterDescriptors.length > 0) {
      recordMapFactory = new MapXmlFactoryPrefilter(recordMapFactory,parameterDescriptors);
    }

    return recordMapFactory;
  }
}

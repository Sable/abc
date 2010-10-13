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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.content.Content;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.Environment;

/**
 * The <code>RecordSubtreeMapFactoryAssembler</code> implements an assembler for
 * assembling <code>RecordSubtreeMapFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordSubtreeMapFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Content[] xmlComponents = new Content[0];

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public MapXmlFactory assemble(ConfigurationContext context) {
    Environment env = new Environment(parameterDescriptors,context.getQnameContext());

    if (xmlComponents.length == 0) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_SPECIALIZATION_REQUIRED,
                                                             context.getElement().getTagName(),
                                                             "sx:content", "sx:transform");
      throw new ServingXmlException(msg);
    }

    MapXmlFactory recordSubtreeMapFactory = new RecordSubtreeMapFactory(env, xmlComponents);
    if (parameterDescriptors.length > 0) {
      recordSubtreeMapFactory = new MapXmlFactoryPrefilter(recordSubtreeMapFactory,parameterDescriptors);
    }
    return recordSubtreeMapFactory;
  }
}


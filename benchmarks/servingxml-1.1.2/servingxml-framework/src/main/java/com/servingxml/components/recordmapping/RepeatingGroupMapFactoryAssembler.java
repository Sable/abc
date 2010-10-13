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
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.NamePath;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.xsltconfig.XsltConfiguration;

/**
 * The <code>RepeatingGroupMapFactoryAssembler</code> implements an assembler for
 * assembling <code>RepeatingGroupMapFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RepeatingGroupMapFactoryAssembler {

  private MapXmlFactory[] fieldMappingFactories = new MapXmlFactory[0];
  private Name fieldName = null;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;

  // Deprecated in ver 0.9.5
  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void setRepeatingGroup(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(MapXmlFactory[] fieldMappingFactories) {
    this.fieldMappingFactories = fieldMappingFactories;
  }
  
  public MapXmlFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (fieldName == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"field");
      throw new ServingXmlException(message);
    }

    if (fieldMappingFactories.length == 0) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                             context.getElement().getTagName(), 
        "literal or element of the sx:fieldMapping family");
      //throw new ServingXmlException(msg);
    }

    MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, fieldMappingFactories);
    MapXmlFactory recordMapFactory = new RepeatingGroupMapFactory(fieldName,rmf);
    if (parameterDescriptors.length > 0) {
      recordMapFactory = new MapXmlFactoryPrefilter(recordMapFactory,parameterDescriptors);
    }
    return recordMapFactory;
  }
}

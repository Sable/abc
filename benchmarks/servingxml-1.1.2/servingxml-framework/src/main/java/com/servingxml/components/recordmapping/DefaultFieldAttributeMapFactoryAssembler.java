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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.NameTest;
import com.servingxml.app.ParameterDescriptor;

/**
 * The <code>DefaultFieldAttributeMapFactoryAssembler</code> implements an assembler for
 * assembling <code>DefaultFieldAttributeMapFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DefaultFieldAttributeMapFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String fieldQnames = "";
  private String exceptQnames = "";

  public void setFields(String fieldQnames) {
    this.fieldQnames = fieldQnames;
  }

  public void setExcept(String exceptQnames) {
    this.exceptQnames = exceptQnames;
  }

  public void setExceptFields(String exceptQnames) {
    this.exceptQnames = exceptQnames;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public MapXmlFactory assemble(ConfigurationContext context) {

    NameTest fields = NameTest.parse(context.getQnameContext(), fieldQnames);
    NameTest except = NameTest.parse(context.getQnameContext(), exceptQnames);

    if (fieldQnames.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "fields");
      throw new ServingXmlException(message);
    }

    MapXmlFactory mapFactory = new DefaultFieldAttributeMapFactory(context.getQnameContext().getPrefixMap(), fields,except);
    if (parameterDescriptors.length > 0) {
      mapFactory = new MapXmlFactoryPrefilter(mapFactory,parameterDescriptors);
    }
    return mapFactory;
  }
}

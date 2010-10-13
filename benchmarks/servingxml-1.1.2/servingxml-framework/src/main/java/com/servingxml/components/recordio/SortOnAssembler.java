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

package com.servingxml.components.recordio;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class SortOnAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name fieldName = Name.EMPTY;
  private String ascending = TrueFalseEnum.TRUE.toString();
  private Rank[] ranks = new Rank[0];

  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void setAscending(String ascending) {
    this.ascending = ascending;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Rank[] ranks) {

    this.ranks = ranks;
  }

  public SortOn assemble(ConfigurationContext context) {

    TrueFalseEnum ascendingEnum;
    try {
      ascendingEnum = TrueFalseEnum.parse(ascending);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "ascending");
      e = e.supplementMessage(message);
      throw e;
    }

    SortOn sortOn = new SortOn(fieldName, ascendingEnum.booleanValue());

    return sortOn;
  }
}


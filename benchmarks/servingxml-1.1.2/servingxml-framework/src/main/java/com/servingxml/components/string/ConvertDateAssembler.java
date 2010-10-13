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

package com.servingxml.components.string;

import org.w3c.dom.Element;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DomHelper;
/**
 * A <tt>ConvertDateAssembler</tt> is an assembler for 
 * assembling 
 * <tt>ConvertDate</tt> objects. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ConvertDateAssembler {


  private Stringable inputFactory = null;
  private String fromFormat = null;
  private String toFormat = "";

  public void setFromFormat(String fromFormat) {
    this.fromFormat = fromFormat;
  }

  public void setToFormat(String toFormat) {
    this.toFormat = toFormat;
  }

  public void injectComponent(Stringable inputFactory) {
    this.inputFactory = inputFactory;
  }

  public StringFactory assemble(final ConfigurationContext context) {

    if (inputFactory == null) {
      Element inputElement = context.getElement();
      String innerText = DomHelper.getInnerText(inputElement);
      if (innerText != null) {
        innerText = innerText.trim();
        if (innerText.length() > 0) {
          inputFactory = new StringLiteralFactory(innerText);
        }
      }
    }

    if (inputFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),
        "sx:stringable based or literal");
      throw new ServingXmlException(message);
    }

    if (fromFormat == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
        "fromFormat");
      throw new ServingXmlException(message);
    }

    StringFactory converter = new ConvertDate(fromFormat,toFormat,inputFactory);

    return converter;
  }
}


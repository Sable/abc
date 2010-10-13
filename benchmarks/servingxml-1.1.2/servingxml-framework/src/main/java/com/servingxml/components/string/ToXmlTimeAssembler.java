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

import java.util.TimeZone;
import javax.xml.datatype.DatatypeFactory;

import org.w3c.dom.Element;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DomHelper;

/**
 * Implements a <tt>ConvertToDateTimeAssembler</tt> for assembling <tt>ConvertToDateTime</tt> objects. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ToXmlTimeAssembler {


  private Stringable inputFactory = null;
  private String fromFormat = null;
  private String inputTimezoneId = null;
  private String outputTimezoneId = null;

  public void setFromFormat(String fromFormat) {
    this.fromFormat = fromFormat;
  }

  public void setFromTimezone(String inputTimezone) {
    this.inputTimezoneId = inputTimezone;
  }

  public void setToTimezone(String outputTimezone) {
    this.outputTimezoneId = outputTimezone;
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
    TimeZone inputTimezone = TimeZone.getDefault();
    if (inputTimezoneId != null) {
      inputTimezone = TimeZone.getTimeZone(inputTimezoneId);
    }
    TimeZone outputTimezone = TimeZone.getDefault();
    if (outputTimezoneId != null) {
      outputTimezone = TimeZone.getTimeZone(outputTimezoneId);
    }

    DatatypeFactory datatypeFactory;
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (javax.xml.datatype.DatatypeConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    StringFactory converter = new ToXmlTime(fromFormat,inputFactory,datatypeFactory,inputTimezone,
                                outputTimezone);

    return converter;
  }
}


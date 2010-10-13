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

package com.servingxml.components.common;

import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;

/**
 * The <code>YesNoEnum</code> represents yes/no
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class YesNoEnum {

  public static final YesNoEnum YES = new YesEnumValue();
  public static final YesNoEnum NO = new NoEnumValue();

  static final String[] values = {YesEnumValue.VALUE, NoEnumValue.VALUE};

  public abstract boolean toBoolean();

  public static YesNoEnum parse(String value) {
    YesNoEnum indicator = null;

    if (value.length() > 0) {
      if (value.equals(YesEnumValue.VALUE)) {
        indicator = YES;
      } else if (value.equals(NoEnumValue.VALUE)) {
        indicator = NO;
      }
    }
    if (indicator == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.VALUE_UNKNOWN,
                         value,
                         StringHelper.toString(values,",","\""));
      throw new ServingXmlException(message);
    }

    return indicator;
  }

  private static class YesEnumValue extends YesNoEnum {

    static final String VALUE = "yes";

    public String toString() {
      return VALUE;
    }

    public boolean toBoolean() {
      return true;
    }
  }

  private static class NoEnumValue extends YesNoEnum {
    static final String VALUE = "no";

    public String toString() {
      return VALUE;
    }

    public boolean toBoolean() {
      return false;
    }
  }
}



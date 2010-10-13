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
 * The <code>TrueFalseEnum</code> represents true/false
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class TrueFalseEnum {

  public static final TrueFalseEnum TRUE = new TrueEnumValue();
  public static final TrueFalseEnum FALSE = new FalseEnumValue();

  static final String[] values = {TrueEnumValue.VALUE, FalseEnumValue.VALUE};

  public abstract boolean booleanValue();

  public static TrueFalseEnum parse(String value) {
    TrueFalseEnum indicator = null;

    if (value.length() > 0) {
      if (value.equals(TrueEnumValue.VALUE) || value.equals("1") || value.equals("yes")) {
        indicator = TRUE;
      } else if (value.equals(FalseEnumValue.VALUE) || value.equals("0") || value.equals("no")) {
        indicator = FALSE;
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

  private static class TrueEnumValue extends TrueFalseEnum {

    static final String VALUE = "true";

    public String toString() {
      return VALUE;
    }

    public boolean booleanValue() {
      return true;
    }
  }

  private static class FalseEnumValue extends TrueFalseEnum {
    static final String VALUE = "false";

    public String toString() {
      return VALUE;
    }

    public boolean booleanValue() {
      return false;
    }
  }
}



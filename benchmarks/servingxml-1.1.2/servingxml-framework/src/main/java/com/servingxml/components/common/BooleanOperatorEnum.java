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
 * The <code>BooleanOperatorEnum</code> represents yes/no
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class BooleanOperatorEnum {

  public static final BooleanOperatorEnum AND = new AndIndicator();
  public static final BooleanOperatorEnum OR = new OrIndicator();

  static final String[] values = {AndIndicator.VALUE, OrIndicator.VALUE};

  public abstract boolean isAnd();
  
  public static BooleanOperatorEnum parse(String value) {
    BooleanOperatorEnum indicator = null;
    
    if (value.length() > 0) {
      if (value.equals(AndIndicator.VALUE)) {
        indicator = AND;
      } else if (value.equals(OrIndicator.VALUE)) {
        indicator = OR;
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
}


class AndIndicator extends BooleanOperatorEnum {

  static final String VALUE = "and";

  public String toString() {
    return VALUE;
  }

  public boolean isAnd() {
    return true;
  }
}

class OrIndicator extends BooleanOperatorEnum {
  static final String VALUE = "or";

  public String toString() {
    return VALUE;
  }

  public boolean isAnd() {
    return false;
  }
}


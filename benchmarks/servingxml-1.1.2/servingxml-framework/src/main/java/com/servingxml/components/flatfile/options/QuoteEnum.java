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

package com.servingxml.components.flatfile.options;

import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;
import com.servingxml.components.common.YesNoEnum;

/**
 * The <code>QuoteEnum</code> represents yes/no
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class QuoteEnum {

  public static final QuoteEnum ALWAYS = new AlwaysEnumValue();
  public static final QuoteEnum NEVER = new NeverEnumValue();
  public static final QuoteEnum AUTO = new AutoEnumValue();

  static final String[] values = {AutoEnumValue.VALUE, AlwaysEnumValue.VALUE, NeverEnumValue.VALUE};

  public abstract boolean auto();
  public abstract boolean always();

  public boolean never() {
    return !(auto() || always());
  }

  public static QuoteEnum parse(String value) {
    QuoteEnum indicator = null;

    if (value.length() > 0) {
      if (value.equals(AutoEnumValue.VALUE)) {
        indicator = AUTO;
      } else if (value.equals(AlwaysEnumValue.VALUE)) {
        indicator = ALWAYS;
      } else if (value.equals(NeverEnumValue.VALUE)) {
        indicator = NEVER;
      } else if (value.equals(YesNoEnum.YES.toString())) {
        indicator = AUTO;
      } else if (value.equals(YesNoEnum.NO.toString())) {
        indicator = NEVER;
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

  private static class AlwaysEnumValue extends QuoteEnum {
    static final String VALUE = "always";

    public String toString() {
      return VALUE;
    }

    public boolean auto() {
      return false;
    }
    public boolean always() {
      return true;
    }
  }

  private static class NeverEnumValue extends QuoteEnum {
    static final String VALUE = "never";

    public String toString() {
      return VALUE;
    }

    public boolean auto() {
      return false;
    }
    public boolean always() {
      return false;
    }
  }

  private static class AutoEnumValue extends QuoteEnum {
    static final String VALUE = "auto";

    public String toString() {
      return VALUE;
    }

    public boolean auto() {
      return true;
    }
    public boolean always() {
      return false;
    }
  }
}



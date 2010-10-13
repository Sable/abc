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

package com.servingxml.util.system;

import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;
import java.util.logging.Level;

/**
 * The <code>LogLevelEnum</code> represents a leg level
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class LogLevelEnum {

  public static final LogLevelEnum ERROR = new ErrorEnumValue();
  public static final LogLevelEnum WARNING = new WarningEnumValue();
  public static final LogLevelEnum NOTIFY = new NotifyEnumValue();
  public static final LogLevelEnum TRACE = new TraceEnumValue();

  static final String[] values = {ErrorEnumValue.VALUE, WarningEnumValue.VALUE, 
    NotifyEnumValue.VALUE, TraceEnumValue.VALUE};

  public abstract void log(RuntimeContext context, String message);

  public static LogLevelEnum parse(String value) {
    LogLevelEnum indicator = null;

    if (value.length() > 0) {
      if (value.equals(ErrorEnumValue.VALUE)) {
        indicator = ERROR;
      } else if (value.equals(WarningEnumValue.VALUE)) {
        indicator = WARNING;
      } else if (value.equals(NotifyEnumValue.VALUE)) {
        indicator = NOTIFY;
      } else if (value.equals(TraceEnumValue.VALUE)) {
        indicator = TRACE;
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

  private static class ErrorEnumValue extends LogLevelEnum {

    static final String VALUE = "error";

    public String toString() {
      return VALUE;
    }

    public void log(RuntimeContext context, String message) {
      context.error(message);
    }
  }

  private static class WarningEnumValue extends LogLevelEnum {
    static final String VALUE = "warning";

    public String toString() {
      return VALUE;
    }

    public void log(RuntimeContext context, String message) {
      context.warning(message);
    }
  }

  private static class NotifyEnumValue extends LogLevelEnum {
    static final String VALUE = "notice";

    public String toString() {
      return VALUE;
    }

    public void log(RuntimeContext context, String message) {
      context.notice(message);
    }
  }

  private static class TraceEnumValue extends LogLevelEnum {
    static final String VALUE = "trace";

    public String toString() {
      return VALUE;
    }

    public void log(RuntimeContext context, String message) {
      context.trace(message,"","",Level.FINE);
    }
  }
}



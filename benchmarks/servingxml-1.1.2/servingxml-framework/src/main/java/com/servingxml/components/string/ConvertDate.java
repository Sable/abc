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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import java.text.ParsePosition;

/**
 * The <code>ConvertDate</code> class implements the <code>StringFactory</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ConvertDate implements StringFactory {

  private final Stringable inputFactory;
  private final String fromFormat;
  private final String toFormat;

  public ConvertDate(String fromFormat, String toFormat, Stringable inputFactory) {

    this.fromFormat = fromFormat;
    this.toFormat = toFormat;
    this.inputFactory = inputFactory;
  }

  public String createString(ServiceContext context, Flow flow) {
    String input = inputFactory.createString(context, flow);
    input = input.trim();
    if (input.length() == 0) {
      return "";
    }
    SimpleDateFormat oldDateFormat;
    try {
      oldDateFormat = new SimpleDateFormat(fromFormat);
    } catch (IllegalArgumentException e) {
      String message = "From date pattern is invalid " + fromFormat;
      throw new ServingXmlException(message, e);
    }

    ParsePosition position = new ParsePosition(0);
    Date date = oldDateFormat.parse(input, position);
    if (date == null) {
      String message = "Illegal input date " + input + ".";
      throw new ServingXmlException(message);
    }
    try {
      DateFormat newDateFormat = toFormat.length() == 0 ? DateFormat.getDateInstance() : new SimpleDateFormat(toFormat);
      String newDateTimeString = newDateFormat.format(date);
      return newDateTimeString;
    } catch (IllegalArgumentException e) {
      String message = "To date pattern is invalid " + toFormat;
      throw new ServingXmlException(message, e);
    }
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder stringBuilder) {
    String s = createString(context,flow);
    stringBuilder.append(s);
  }
}


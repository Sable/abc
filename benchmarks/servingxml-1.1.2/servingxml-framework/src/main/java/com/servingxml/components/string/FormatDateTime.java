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
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.Calendar;
import javax.xml.datatype.DatatypeConstants;
import java.util.TimeZone;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;

/**
 * The <code>FormatDateTime</code> class implements the <code>Stringable</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FormatDateTime implements StringFactory {

  private final Stringable inputFactory;
  private final String toFormat;
  private final DatatypeFactory datatypeFactory;

  public FormatDateTime(String toFormat, Stringable inputFactory, DatatypeFactory datatypeFactory) {

    this.toFormat = toFormat;
    this.inputFactory = inputFactory;
    this.datatypeFactory = datatypeFactory;
  }

  public String createString(ServiceContext context, Flow flow) {

    String input = "";
    try {
      input = inputFactory.createString(context, flow);
      XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(input);
      DateFormat toDateFormat = toFormat.length() == 0 ? DateFormat.getDateInstance() : new SimpleDateFormat(toFormat);
      GregorianCalendar calendar = xmlCalendar.toGregorianCalendar();
      Date date = calendar.getTime();
      String formattedDateTime = toDateFormat.format(date);
      return formattedDateTime;
    } catch (IllegalArgumentException e) {
      String message;
      if (input.length() == 0) {
        message = "Empty input date.";
      } else {
        message = "Illegal input date " + input + ".";
      }
      throw new ServingXmlException(message, e);
    }
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder stringBuilder) {
    String s = createString(context,flow);
    stringBuilder.append(s);
  }
}


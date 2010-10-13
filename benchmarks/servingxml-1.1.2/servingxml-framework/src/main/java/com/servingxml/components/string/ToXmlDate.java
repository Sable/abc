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
 * The <code>ConvertDate</code> class implements the <code>StringFactory</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ToXmlDate implements StringFactory {

  private final Stringable inputFactory;
  private final String fromFormat;
  private final DatatypeFactory datatypeFactory;
  private final TimeZone fromTimezone;
  private final TimeZone toTimezone;

  public ToXmlDate(String fromFormat, Stringable inputFactory, DatatypeFactory datatypeFactory) {

    this.fromFormat = fromFormat;
    this.inputFactory = inputFactory;
    this.datatypeFactory = datatypeFactory;
    this.fromTimezone = TimeZone.getDefault();
    this.toTimezone = TimeZone.getDefault();
  }

  public ToXmlDate(String fromFormat, Stringable inputFactory, DatatypeFactory datatypeFactory,
    TimeZone fromTimezone) {

    this.fromFormat = fromFormat;
    this.inputFactory = inputFactory;
    this.datatypeFactory = datatypeFactory;
    this.fromTimezone = fromTimezone;
    this.toTimezone = TimeZone.getDefault();
  }

  public ToXmlDate(String fromFormat, Stringable inputFactory, DatatypeFactory datatypeFactory,
    TimeZone fromTimezone, TimeZone toTimezone) {

    this.fromFormat = fromFormat;
    this.inputFactory = inputFactory;
    this.datatypeFactory = datatypeFactory;
    this.fromTimezone = fromTimezone;
    this.toTimezone = toTimezone;
  }

  public String createString(ServiceContext context, Flow flow) {

    String input = inputFactory.createString(context, flow);
    input = input.trim();
    if (input.length() == 0) {
      return "";
    }
    try {
      SimpleDateFormat oldDateFormat = new SimpleDateFormat(fromFormat);

      Date date = oldDateFormat.parse(input);
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(date.getTime());

      if (!fromTimezone.equals(TimeZone.getDefault())) {
        GregorianCalendar newCalendar = new GregorianCalendar(fromTimezone);
        newCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        newCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        newCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        newCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        newCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        newCalendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
        newCalendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
        calendar = newCalendar;
      }                 
      
      if (!toTimezone.equals(fromTimezone)) {
        GregorianCalendar newCalendar = new GregorianCalendar(toTimezone);
        newCalendar.setTimeInMillis(calendar.getTimeInMillis());
        calendar = newCalendar;
      }

      int year = calendar.get(Calendar.YEAR);
      int month = calendar.get(Calendar.MONTH) + (DatatypeConstants.JANUARY - Calendar.JANUARY);
      int day = calendar.get(Calendar.DAY_OF_MONTH);
      int hour = calendar.get(Calendar.HOUR_OF_DAY);
      int minute = calendar.get(Calendar.MINUTE);
      int second = calendar.get(Calendar.SECOND);
      int millisecond = calendar.get(Calendar.MILLISECOND);
      int zoneOffset = calendar.get(Calendar.ZONE_OFFSET) / (1000*60);

      XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar();

      xmlCalendar.setYear(year);
      xmlCalendar.setMonth(month);
      xmlCalendar.setDay(day);
      //xmlCalendar.setTimezone(zoneOffset);

      String newDateTimeString = xmlCalendar.toString();
      return newDateTimeString;
    } catch (java.text.ParseException e) {
      String message = "Error parsing date " + input + ".  " + e.getMessage();
      throw new ServingXmlException(message, e);
    }
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder stringBuilder) {
    String s = createString(context,flow);
    stringBuilder.append(s);
  }
}


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
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;

/**
 * The <code>CurrentDate</code> class implements the <code>StringFactory</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CurrentDate implements StringFactory {

  private final DatatypeFactory datatypeFactory;

  public CurrentDate(DatatypeFactory datatypeFactory) {

    this.datatypeFactory = datatypeFactory;
  }

  public String createString(ServiceContext context, Flow flow) {

    GregorianCalendar calendar = new GregorianCalendar();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + (DatatypeConstants.JANUARY - Calendar.JANUARY);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    //int offsetInMilli = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
    //int timezoneOffset = offsetInMilli/(60 * 1000);

    XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar();
    xmlCalendar.setYear(year);
    xmlCalendar.setMonth(month);
    xmlCalendar.setDay(day);
    //xmlCalendar.setTimezone(timezoneOffset);

    return xmlCalendar.toString();
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder stringBuilder) {
    String s = createString(context,flow);
    stringBuilder.append(s);
  }
}


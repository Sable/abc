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

package com.servingxml.util.record;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;


public class DateTimeData {
  private final BigInteger eonAndYear;
  private final int year;
  private final int month;
  private final int day;
  private final int hour;
  private final int minute;
  private final int second;
  private final int millisecond;
  private final BigDecimal fractionalSecond;
  private final int timezoneOffset;

  public DateTimeData(XMLGregorianCalendar calendar) {
    this.eonAndYear = calendar.getEonAndYear();
    this.year = calendar.getYear();
    this.month = calendar.getMonth();
    this.day = calendar.getDay();
    this.hour = calendar.getHour();
    this.minute = calendar.getMinute();
    this.second = calendar.getSecond();
    this.millisecond = calendar.getMillisecond();
    this.fractionalSecond = calendar.getFractionalSecond();
    this.timezoneOffset = calendar.getTimezone();
  }

  public DateTimeData(int year, int month, int day) {
    this.eonAndYear = BigInteger.valueOf((long)year);
    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = DatatypeConstants.FIELD_UNDEFINED;
    this.minute = DatatypeConstants.FIELD_UNDEFINED;
    this.second = DatatypeConstants.FIELD_UNDEFINED;
    this.millisecond = DatatypeConstants.FIELD_UNDEFINED;
    this.fractionalSecond = null;
    this.timezoneOffset = DatatypeConstants.FIELD_UNDEFINED;
  }

  public DateTimeData(int year, int month, int day, int hour, int minute, int second, int millisecond,
    int timezoneOffset) {
    this.eonAndYear = BigInteger.valueOf((long)year);
    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = hour;
    this.minute = minute;
    this.second = second;
    this.millisecond = millisecond;
    this.fractionalSecond = null;
    this.timezoneOffset = timezoneOffset;
  }

  public String getString(DatatypeFactory datatypeFactory) {
    XMLGregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(eonAndYear,
      month, day, hour, minute, second, fractionalSecond, timezoneOffset);

    String s = calendar.toString();

    return s;
  }

  public static DateTimeData fromSqlTimestamp(Timestamp timestamp) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(timestamp.getTime());
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + (DatatypeConstants.JANUARY - Calendar.JANUARY);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    int second = calendar.get(Calendar.SECOND);
    int millisecond = calendar.get(Calendar.MILLISECOND);

    int offsetInMilli = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
    int timezoneOffset = offsetInMilli/(60 * 1000);

    return new DateTimeData(year,month,day,hour,minute,second,millisecond,timezoneOffset);
  }

  public static DateTimeData fromSqlTime(Time time) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(time.getTime());
    int year = DatatypeConstants.FIELD_UNDEFINED;
    int month = DatatypeConstants.FIELD_UNDEFINED;
    int day = DatatypeConstants.FIELD_UNDEFINED;
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    int second = calendar.get(Calendar.SECOND);
    int millisecond = calendar.get(Calendar.MILLISECOND);

    int offsetInMilli = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
    int timezoneOffset = offsetInMilli/(60 * 1000);

    return new DateTimeData(year,month,day,hour,minute,second,millisecond,timezoneOffset);
  }

  public static DateTimeData fromSqlDate(Date date) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(date.getTime());
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + (DatatypeConstants.JANUARY - Calendar.JANUARY);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return new DateTimeData(year,month,day);
  }

  public Timestamp getSqlTimestamp() {
    GregorianCalendar calendar;

    if (timezoneOffset != DatatypeConstants.FIELD_UNDEFINED) {
      int offsetInMilli = timezoneOffset * 60 * 1000;
      TimeZone timeZone = new SimpleTimeZone(offsetInMilli,"xxx");
      calendar = new GregorianCalendar(timeZone);
    } else {
      calendar = new GregorianCalendar();
    }

    if (year != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.YEAR, year);
    }
    if (month != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MONTH, month + (Calendar.JANUARY - DatatypeConstants.JANUARY));
    }
    if (day != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.DAY_OF_MONTH, day);
    }
    if (hour != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    if (minute != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MINUTE, minute);
    }
    if (second != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.SECOND, second);
    }
    if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MILLISECOND, millisecond);
    }
    Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

    return timestamp;
  }

  public Date getSqlDate() {
    GregorianCalendar calendar;

    if (timezoneOffset != DatatypeConstants.FIELD_UNDEFINED) {
      int offsetInMilli = timezoneOffset * 60 * 1000;
      TimeZone timeZone = new SimpleTimeZone(offsetInMilli,"xxx");
      calendar = new GregorianCalendar(timeZone);
    } else {
      calendar = new GregorianCalendar();
    }

    if (year != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.YEAR, year);
    }
    if (month != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MONTH, month + (Calendar.JANUARY - DatatypeConstants.JANUARY));
    }
    if (day != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.DAY_OF_MONTH, day);
    }
    if (hour != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    if (minute != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MINUTE, minute);
    }
    if (second != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.SECOND, second);
    }
    if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MILLISECOND, millisecond);
    }
    Date date = new Date(calendar.getTimeInMillis());

    return date;
  }

  public Time getSqlTime() {
    GregorianCalendar calendar;

    if (timezoneOffset != DatatypeConstants.FIELD_UNDEFINED) {
      int offsetInMilli = timezoneOffset * 60 * 1000;
      TimeZone timeZone = new SimpleTimeZone(offsetInMilli,"xxx");
      calendar = new GregorianCalendar(timeZone);
    } else {
      calendar = new GregorianCalendar();
    }

    if (year != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.YEAR, year);
    }
    if (month != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MONTH, month + (Calendar.JANUARY - DatatypeConstants.JANUARY));
    }
    if (day != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.DAY_OF_MONTH, day);
    }
    if (hour != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.HOUR_OF_DAY, hour);
    }
    if (minute != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MINUTE, minute);
    }
    if (second != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.SECOND, second);
    }
    if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
      calendar.set(Calendar.MILLISECOND, millisecond);
    }
    Time time = new Time(calendar.getTimeInMillis());

    return time;
  }
}

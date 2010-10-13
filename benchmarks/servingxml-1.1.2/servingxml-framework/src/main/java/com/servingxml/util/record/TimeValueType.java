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

import java.sql.Types;
import java.sql.Time;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

import com.servingxml.util.ServingXmlException;

public class TimeValueType implements ValueType {
  private final DatatypeFactory datatypeFactory;

  public TimeValueType(DatatypeFactory datatypeFactory) {
    this.datatypeFactory = datatypeFactory;
  }

  public Object[] fromStringArray(String[] sa) {
    try {
      DateTimeData[] a = new DateTimeData[sa.length];
      for (int i = 0; i < sa.length; ++i) {
        XMLGregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(sa[i]);
        DateTimeData value = new DateTimeData(calendar);
        a[i] = value;
      }
      return a;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public Object fromString(String s) {
    try {
      XMLGregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(s);
      DateTimeData value = new DateTimeData(calendar);
      return value;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public String toString(Object o) {
    String s = "";
    if (o != null) {
      DateTimeData value = (DateTimeData)o;
      s = value.getString(datatypeFactory);
    }
    return s;
  }

  public Object getSqlValue(Object o) {
    Time time = null;
    if (o != null) {
      DateTimeData data = (DateTimeData)o;
      time = data.getSqlTime();
    }
    return time;
  }

  public int getSqlType() {
    return Types.TIME;
  }
}

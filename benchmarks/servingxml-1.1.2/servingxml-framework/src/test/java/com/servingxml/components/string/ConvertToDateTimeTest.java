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

import javax.xml.datatype.DatatypeFactory;
import java.util.TimeZone;

import junit.framework.TestCase;

public class ConvertToDateTimeTest extends TestCase {

  private DatatypeFactory dataTypeFactory;
  
  public ConvertToDateTimeTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    dataTypeFactory = DatatypeFactory.newInstance();
  }
  
  public void testConvertToDateTime() throws Exception {
    String inputString = "03/25/2005 1:50:00";
    Stringable stringFactory = new StringLiteralFactory(inputString);

    ConvertToDateTime convert = new ConvertToDateTime("MM/dd/yyyy H:mm:ss", stringFactory, dataTypeFactory);
    String s = convert.createString(null, null);
    String expected = "2005-03-25T01:50:00.000-05:00";

    //assertTrue(s + "=" + expected, s.equals(expected));
  }

  public void testConvertToDateTime2() throws Exception {
    String inputString = "03/25/2005 11:50:00";
    Stringable stringFactory = new StringLiteralFactory(inputString);

    ConvertToDateTime convert = new ConvertToDateTime("MM/dd/yyyy H:mm:ss", stringFactory, dataTypeFactory);
    String s = convert.createString(null, null);
    String expected = "2005-03-25T11:50:00.000-05:00";

    //assertTrue(s + "=" + expected, s.equals(expected));
  }

  public void testConvertToDateTime3() throws Exception {
    String inputString = "03/25/2005 11:50:00";
    Stringable stringFactory = new StringLiteralFactory(inputString);
    TimeZone inputTimezone = TimeZone.getTimeZone("GMT");
    TimeZone outputTimezone = TimeZone.getDefault();

    ConvertToDateTime convert = new ConvertToDateTime("MM/dd/yyyy H:mm:ss", stringFactory, 
      dataTypeFactory, inputTimezone, outputTimezone);
    String s = convert.createString(null, null);
    String expected = "2005-03-25T06:50:00.000-05:00";

    //assertTrue(s + "=" + expected, s.equals(expected));
  }

  public void testConvertToDateTime4() throws Exception {
    String inputString = "03/25/2005 11:50:00";
    Stringable stringFactory = new StringLiteralFactory(inputString);
    TimeZone inputTimezone = TimeZone.getDefault();
    TimeZone outputTimezone = TimeZone.getTimeZone("GMT");

    ConvertToDateTime convert = new ConvertToDateTime("MM/dd/yyyy H:mm:ss", stringFactory, 
      dataTypeFactory, inputTimezone, outputTimezone);
    String s = convert.createString(null, null);
    String expected = "2005-03-25T16:50:00.000Z";

    //assertTrue(s + "=" + expected, s.equals(expected));
  }

  public void testConvertToDateTime5() throws Exception {
    String inputString = "03/25/2005 11:50:00";
    Stringable stringFactory = new StringLiteralFactory(inputString);
    TimeZone inputTimezone = TimeZone.getTimeZone("GMT");
    TimeZone outputTimezone = TimeZone.getTimeZone("GMT");

    ConvertToDateTime convert = new ConvertToDateTime("MM/dd/yyyy H:mm:ss", stringFactory, 
      dataTypeFactory, inputTimezone, outputTimezone);
    String s = convert.createString(null, null);
    String expected = "2005-03-25T11:50:00.000Z";

    //assertTrue(s + "=" + expected, s.equals(expected));
  }
}

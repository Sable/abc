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

package com.servingxml.util;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class StringHelperTest extends TestCase {

  public StringHelperTest(String name) {
    super(name);
  }

  protected void setUp() {
  }

  public void testTrimLeading() throws Exception {
    String expected = "abcdef ";
    String s1 = "   " + expected;
    String value1 = StringHelper.trimLeading(s1);
    assertTrue(value1 + "=" + expected + ".", value1.equals(expected));
    String value2 = StringHelper.trimLeading(expected);
    assertTrue(value2 + "=" + expected + ".", value2.equals(expected));
  }

  public void testTrimTrailing() throws Exception {
    String expected = " abcdef";
    String s1 = expected + "   ";
    String value1 = StringHelper.trimTrailing(s1);
    assertTrue(value1 + "=" + expected + ".", value1.equals(expected));
    String value2 = StringHelper.trimTrailing(expected);
    assertTrue(value2 + "=" + expected + ".", value2.equals(expected));
  }

  public void testTrimEmpty() throws Exception {
    String expected = "";
    String s1 = "   " + expected;
    String value1 = StringHelper.trimLeading(s1);
    assertTrue(value1 + "=" + expected + ".", value1.equals(expected));
    String value2 = StringHelper.trimLeading(expected);
    assertTrue(value2 + "=" + expected + ".", value2.equals(expected));

    String s2 = expected + "   ";
    String value3 = StringHelper.trimTrailing(s2);
    assertTrue(value3 + "=" + expected + ".", value3.equals(expected));
    String value4 = StringHelper.trimTrailing(expected);
    assertTrue(value4 + "=" + expected + ".", value4.equals(expected));

  }

  public void testContains() throws Exception {
    boolean expected = true;
    char[] delim = {','};
    boolean actual = StringHelper.contains("TANZIANIA, UNITED REPUBLIC OF",delim);
    assertTrue(""+actual + "=" + expected + ".",actual );
  }

  public void testContains2() throws Exception {
    boolean expected = true;
    char[] delim = {','};
    boolean actual = StringHelper.contains("UNITED ARAB EMIRATES",delim);
    assertTrue(""+actual + "=" + expected + ".",!actual );
  }

  public void testContains3() throws Exception {
    boolean expected = true;
    char[] delim = {','};
    boolean actual = StringHelper.contains("TANZIANIA UNITED REPUBLIC OF,",delim);
    assertTrue(""+actual + "=" + expected + ".",actual );
  }

  public void testContains4() throws Exception {
    boolean expected = true;
    char[] delim = {','};
    boolean actual = StringHelper.contains(",TANZIANIA UNITED REPUBLIC OF",delim);
    assertTrue(""+actual + "=" + expected + ".",actual );
  }

  public void testConstructName1() throws Exception {
    String expected = "_1";
    String result = StringHelper.constructNameFromValue("1");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName2() throws Exception {
    String expected = "a";
    String result = StringHelper.constructNameFromValue("a ");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName3() throws Exception {
    String expected = "a";
    String result = StringHelper.constructNameFromValue(" a");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName4() throws Exception {
    String expected = "a_b";
    String result = StringHelper.constructNameFromValue("a b");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName5() throws Exception {
    String expected = "a_b";
    String result = StringHelper.constructNameFromValue("a  b");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName6() throws Exception {
    String expected = "c_d";
    String result = StringHelper.constructNameFromValue("c_d");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName7() throws Exception {
    String expected = "_cd";
    String result = StringHelper.constructNameFromValue("_cd");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName8() throws Exception {
    String expected = "c-d";
    String result = StringHelper.constructNameFromValue("c-d");
    assertTrue(result+"="+expected,result.equals(expected));
  }

  public void testConstructName9() throws Exception {
    String expected = "c.d";
    String result = StringHelper.constructNameFromValue("c.d");
    assertTrue(result+"="+expected,result.equals(expected));
  }
}                    


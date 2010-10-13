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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import samples.books.BookCatalog;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DelimiterTest extends TestCase {

  public DelimiterTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testOccurIn1() throws Exception {
    String s = "\r\n";

    Delimiter delimiter = new RecordDelimiter("\r\n", true, true);
    assertTrue(s, delimiter.occursIn(s));
  }

  public void testOccurIn2() throws Exception {
    String s = "\r";

    Delimiter delimiter = new RecordDelimiter("\r\n", true, true);
    assertTrue(s, !delimiter.occursIn(s));
  }

  public void testOccurIn3() throws Exception {
    String left = "{";
    String right = "}";

    Delimiter delimiter = new RecordDelimiter(new StartEndSeparator(left, right), true, true);
    assertTrue(left, delimiter.occursIn(left));
    assertTrue(right, delimiter.occursIn(right));
  }

  public void testOccurIn4() throws Exception {
    String left = "{";
    String right = "}";

    Delimiter delimiter = new RecordDelimiter(new StartEndSeparator("{-", "-}"), true, true);
    assertTrue(left, !delimiter.occursIn(left));
    assertTrue(right, !delimiter.occursIn(right));
  }

  public void testOccurIn6() throws Exception {
    String s0 = "";
    String s1 = "-";
    String s2 = "--";

    Delimiter delimiter = new RecordDelimiter(new DefaultSeparator("\r\n","--","\r\n--"), true, true);
    assertTrue(s0, !delimiter.occursIn(s0));
    assertTrue(s1, !delimiter.occursIn(s1));
    assertTrue(s2, delimiter.occursIn(s2));
  }
}                    


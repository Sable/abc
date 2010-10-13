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

import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class UrlHelperTest extends TestCase {

  public UrlHelperTest(String name) {
    super(name);
  }

  protected void setUp() {
  }

  public void xtestCreateUrl() throws Exception {
    URL url = UrlHelper.createUrl("servingxml-tests.jar",null);
    String dir = System.getProperty("user.dir");
    if (!dir.endsWith("/")) {
      dir = dir + "/";
    }
    String expected = dir+"servingxml-tests.jar";
    assertTrue(url.toString()+"="+expected,url.toString().equals(expected));
  }

  public void testCreateUrl2() throws Exception {
    URL url = UrlHelper.createUrl("framework","http://www.servingxml.com/");
    String expected = "http://www.servingxml.com/framework";
    assertTrue(url.toString()+"="+expected,url.toString().equals(expected));
  }

  public void xtestCreateUrl3() throws Exception {
    URL url = UrlHelper.createUrl("framework","file:///c:Project Files/");
    String expected = "file:/c:Project%20Files/framework";
    assertTrue(url.toString()+"="+expected,url.toString().equals(expected));
  }

  public void xtestCreateUrl4() throws Exception {
    URL url = UrlHelper.createUrl(" frame work ","file:///c:Project Files/");
    String expected = "file:/c:Project%20Files/%20frame%20work%20";
    assertTrue(url.toString()+"="+expected,url.toString().equals(expected));
  }
}                    


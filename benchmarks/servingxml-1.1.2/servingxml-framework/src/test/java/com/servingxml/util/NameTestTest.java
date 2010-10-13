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

public class NameTestTest extends TestCase {

  private static String MYAPP_NS_URI="http://mycompany.com/mynames/";
  private static String MYAPP_NS_PREFIX="myns";

  private MutableNameTable baseNameTable;

  public NameTestTest(String name) {
    super(name);
  }

  protected void setUp() {
    baseNameTable = new NameTableImpl(1,1);
    int requestId = baseNameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"request");
  }

  public void testLocalNameToken() throws Exception {
    QnameContext context = new SimpleQnameContext();

    String tokens="blue green purple";

    NameTest nameToken = NameTest.parse(context,tokens);

    assertTrue("blue", nameToken.matches("","blue"));
    assertTrue("green", nameToken.matches("","green"));
    assertTrue("purple", nameToken.matches("","purple"));
    assertTrue("red", !nameToken.matches("","red"));
  }

  public void testAnyNamespace() throws Exception {
    QnameContext context = new SimpleQnameContext();

    String tokens="*:blue *:green *:purple";

    NameTest nameToken = NameTest.parse(context,tokens);

    assertTrue("blue", nameToken.matches("myNamespace","blue"));
    assertTrue("green", nameToken.matches("myNamespace","green"));
    assertTrue("purple", nameToken.matches("myNamespace","purple"));
    assertTrue("red", !nameToken.matches("myNamespace","red"));
  }

  public void testAny() throws Exception {
    QnameContext context = new SimpleQnameContext();

    String tokens="*";

    NameTest nameToken = NameTest.parse(context,tokens);

    assertTrue("blue", nameToken.matches("myNamespace","blue"));
    assertTrue("green", nameToken.matches("myNamespace","green"));
    assertTrue("purple", nameToken.matches("myNamespace","purple"));
    assertTrue("red", nameToken.matches("myNamespace","red"));
  }

  public void testEmpty() throws Exception {
    QnameContext context = new SimpleQnameContext();

    String tokens="";

    NameTest nameToken = NameTest.parse(context,tokens);

    assertTrue("blue", !nameToken.matches("myNamespace","blue"));
    assertTrue("green", !nameToken.matches("myNamespace","green"));
    assertTrue("purple", !nameToken.matches("myNamespace","purple"));
    assertTrue("red", !nameToken.matches("myNamespace","red"));
  }
}                    


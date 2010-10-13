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

import java.io.StringReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;


/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class PrefixMapTest extends TestCase {

  public PrefixMapTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testPrefixMapping() throws Exception {
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping("eg1","http://www.example1.com");
    prefixMap.setPrefixMapping("eg2","http://www.example2.com");
    prefixMap.setPrefixMapping("eg3","http://www.example3.com");
    prefixMap.setPrefixMapping("eg4","http://www.example4.com");
    prefixMap.setPrefixMapping("eg5","http://www.example5.com");
    prefixMap.setPrefixMapping("eg6","http://www.example6.com");
    prefixMap.setPrefixMapping("eg7","http://www.example7.com");
    prefixMap.setPrefixMapping("eg8","http://www.example8.com");
    prefixMap.setPrefixMapping("eg9","http://www.example9.com");
    prefixMap.setPrefixMapping("eg10","http://www.example10.com");
    prefixMap.setPrefixMapping("eg11","http://www.example11.com");
    prefixMap.setPrefixMapping("eg11","http://www.example11a.com");

    assertTrue("example11", !prefixMap.containsPrefixMapping("eg11", "http://www.example11.com" ));
    assertTrue("example11a", prefixMap.containsPrefixMapping("eg11", "http://www.example11a.com" ));

    PrefixMapImpl prefixMap2 = new PrefixMapImpl(prefixMap);

    assertTrue("2-example11", !prefixMap2.containsPrefixMapping("eg11", "http://www.example11.com" ));
    assertTrue("2-example11a", prefixMap2.containsPrefixMapping("eg11", "http://www.example11a.com" ));

    assertTrue("2-example9", prefixMap2.getNamespaceUri("eg9").equals("http://www.example9.com"));
    assertTrue("2-example8", prefixMap2.getPrefix("http://www.example8.com").equals("eg8"));

    String expected = "xmlns:eg11=\"http://www.example11a.com\" xmlns:eg4=\"http://www.example4.com\" xmlns:eg7=\"http://www.example7.com\" xmlns:eg9=\"http://www.example9.com\" xmlns:eg8=\"http://www.example8.com\" xmlns:eg6=\"http://www.example6.com\" xmlns:eg10=\"http://www.example10.com\" xmlns:eg5=\"http://www.example5.com\" xmlns:eg2=\"http://www.example2.com\" xmlns:eg3=\"http://www.example3.com\" xmlns:eg1=\"http://www.example1.com\"";
    String s = prefixMap2.getPrefixDeclarationString().trim();
    assertTrue(s+"="+expected,s.equals(expected));
  }

  public void testDefaults() throws Exception {
    PrefixMapImpl prefixMap = new PrefixMapImpl();

    assertTrue("empty namespaceUri", prefixMap.getPrefix("").equals(""));

    //assertTrue("not found namespaceUri", prefixMap.getPrefix("xxx") == null);
  }

}


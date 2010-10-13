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

package com.servingxml.util.xml;

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.Name;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.PrefixMap;

public class DomNameContextTest extends TestCase {
  private static String MY_NAMESPACE_URL = "http://mycompany.com/mynames/";
  private static String MY_NAMESPACE_PREFIX = "myns";
  private static String MY_NAMESPACE2_URL = "http://www.servingxml.com/MyNamespace2";
  private static String MY_NAMESPACE2_PREFIX = "myns2";
  
  private static String resourcesString = 
    "<sx:resources xmlns:sx=\"http://www.servingxml.com/ServingXML\" xmlns:myns=\"http://mycompany.com/mynames/\" ns=\"" + MY_NAMESPACE_URL + "\">" +
    "</sx:resources>";
  
  private static String contentString = 
    "<sx:resources xmlns:sx=\"http://www.servingxml.com/ServingXML\" xmlns:myns=\"http://mycompany.com/mynames/\">" +
      "<sx:expiryOptions caching=\"yes\" revalidate=\"full\"/>" + 
      "<sx:document name=\"pulp\" href=\"books/pulp.xml\"/>" + 
      "<sx:document name=\"pulp2\" ns=\"" + MY_NAMESPACE2_URL + "\" href=\"books/pulp.xml\"/>" +
    "</sx:resources>";
  
  private Element resourcesElement;
  private Element contentElement;
  private MutableNameTable nameTable = new NameTableImpl();
  
  public DomNameContextTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Turn off validation, and turn on namespaces
    factory.setValidating(false);
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    
    InputSource resourcesSource = new InputSource(new StringReader(resourcesString));
    Document resourcesDoc = builder.parse(resourcesSource);
    resourcesElement = resourcesDoc.getDocumentElement();
    
    InputSource contentSource = new InputSource(new StringReader(contentString));
    Document contentDoc = builder.parse(contentSource);
    contentElement = contentDoc.getDocumentElement();
  }
  
  public void testResourcesContext() throws Exception {
    DomQnameContext resourcesContext = new DomQnameContext(nameTable, resourcesElement);
    Name name = resourcesContext.createName("myns:pulp");
    Name expected = new QualifiedName(MY_NAMESPACE_URL,"pulp");
    assertTrue(name.toString() + " = " + expected,name.equals(expected));
  }
  
  public void testContentContext() throws Exception {
    DomQnameContext resourcesContext = new DomQnameContext(nameTable, resourcesElement);
    DomQnameContext contentContext = new DomQnameContext(resourcesContext, contentElement);
    Name name = contentContext.createName("myns:pulp");
    Name expected = new QualifiedName(MY_NAMESPACE_URL,"pulp");
    assertTrue(name.toString() + " = " + expected,name.equals(expected));
    
    Iterator iter = DomHelper.createChildElementIterator(contentElement);
    while (iter.hasNext()) {
      Element childElement = (Element)iter.next();
      DomQnameContext childContext = new DomQnameContext(contentContext, childElement);
      if (childElement.getLocalName().equals("pulp")) {
        Name n = childContext.createName("pulp");
        Name e = new QualifiedName(MY_NAMESPACE_URL,"pulp");
        assertTrue(n.toString() + " = " + e,n.equals(e));
      } else if (childElement.getLocalName().equals("pulp2")) {
        Name n = childContext.createName("pulp2");
        Name e = new QualifiedName(MY_NAMESPACE2_URL,"pulp2");
        assertTrue(n.toString() + " = " + e,n.equals(e));
      }
    }
  }
}

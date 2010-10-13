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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;

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

public class DomIteratorTest extends TestCase {
  private static final String myNamespaceUri = "http://mycompany.com/mynames/";

  public DomIteratorTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testAttributeIterator() throws Exception {
    String xmlString = "<sx:replace xmlns:myns=\"http://mycompany.com/mynames/\"  ns=\"http://mycompany.com/mynames/\" xmlns:sx=\"http://www.servingxml.com/ServingXML\" pattern=\"(.+)\">$1</sx:replace>";

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    builderFactory.setNamespaceAware(true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(xmlString));
    Document doc = builder.parse(inputSource);
    Element root = doc.getDocumentElement();

    DomIterator.AttributeCommand command = new DomIterator.AttributeCommand() {
      public void doPrefixMapping(Element element, String prefix, String namespaceUri) {
        //System.out.println ("prefix = " + prefix + ", namespaceUri = " + namespaceUri);
      }
      public void doAttribute(Element element, String namespaceUri, String localName,
      String qname, String value) {
        //System.out.println ("namespaceUri = " + namespaceUri +
        //  ", localName = " + localName + ", qname = " + qname + ", value = " + value);

      }
      public void doEntityReference(Element element, EntityReference entityReference) {
        //System.out.println ("Got entityRef");
      }
    };

    DomIterator.toEveryAttribute(root,command);
  }
}


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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.Templates;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.ioc.resources.MutableResourceTable;
import com.servingxml.ioc.resources.ResourceTableImpl;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XPathExpressionTest extends TestCase {
  private static final String myNamespaceUri = "http://mycompany.com/mynames/";
  
  private MutableNameTable nameTable;
  private MutableResourceTable resourceTable;
  private Element iocContainerElement;

  public XPathExpressionTest(String name) {
    super(name);
  }
  protected void setUp() throws Exception {
    
    nameTable = new NameTableImpl();
    resourceTable = new ResourceTableImpl();

    String componentString = "<sx:resources xmlns:myns=\"http://mycompany.com/mynames/\"  ns=\"http://mycompany.com/mynames/\" xmlns:sx=\"http://www.servingxml.com/ServingXML\"></sx:resources>";

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    builderFactory.setNamespaceAware(true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(componentString));
    Document doc = builder.parse(inputSource);
    iocContainerElement = doc.getDocumentElement();
  }

  public void testXPathExpression() throws Exception {
      String style = "<xsl:transform version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><xsl:template match=\"customer\"><MessageData></MessageData></xsl:template></xsl:transform>";
      StringReader reader = new StringReader(style);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setValidating(false);
      builderFactory.setNamespaceAware(true);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();

      InputSource inputSource = new InputSource(reader);
      Document doc = builder.parse(inputSource);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      DOMResult result = new DOMResult();
      transformer.transform(source,result);

      TransformerFactory transformerFactory2 =  transformerFactory; //TransformerFactory.newInstance();
      Transformer transformer2 = transformerFactory2.newTransformer();
      DOMSource source2 = new DOMSource(result.getNode());
      DOMResult result2 = new DOMResult();
      transformer2.transform(source2,result2);
      Templates templates = transformerFactory2.newTemplates(source);
      Transformer transformer3 = templates.newTransformer();

      SAXTransformerFactory saxFactory = (SAXTransformerFactory)transformerFactory;  //TransformerFactory.newInstance();

      //  TODO:  initialize
      TransformerHandler handler = saxFactory.newTransformerHandler();
      DOMResult result3 = new DOMResult();
      handler.setResult(result3);
      //Transformer transformer3 = handler.getTransformer();
      SAXResult saxResult = new SAXResult(handler);
      transformer3.transform(source,result3);
  }
}


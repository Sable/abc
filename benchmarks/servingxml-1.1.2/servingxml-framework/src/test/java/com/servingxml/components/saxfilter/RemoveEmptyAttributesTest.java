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

package com.servingxml.components.saxfilter;

import java.io.Writer;
import java.io.StringWriter;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;
import com.servingxml.util.record.Record;
import com.servingxml.util.NameTest;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.io.saxsource.StreamSourceSaxSource;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RemoveEmptyAttributesTest extends TestCase {

  private static final String suppressNone = "<doc><name>John</name><age/><sex/></doc>";
  private static final String suppressAnyEmpty = "<doc><name>John</name></doc>";
  private static final String suppressEmptyAge = "<doc><name>John</name><sex/></doc>";
  private static final String suppressEmptyAgeAndSex = "<doc><name>John</name></doc>";
  private SimpleQnameContext nameContext;

  public RemoveEmptyAttributesTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping("ns1","http://www.namespace.com");
    nameContext = new SimpleQnameContext(prefixMap);
  }

  public void testRemoveEmptyIdAttribute() throws Exception {
    String xmlString = "<doc id=\"\"><name>John</name><age/><sex/></doc>";
    StreamSource streamSource = new StringStreamSource(xmlString);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "*";
    String attributeNames = "id";
    NameTest elementNameTest = NameTest.parse(nameContext, elementNames);
    NameTest attributeNameTest = NameTest.parse(nameContext, attributeNames);
    XMLFilter xmlFilter = new RemoveEmptyAttributes(elementNameTest, NameTest.NONE, attributeNameTest, NameTest.NONE);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String s = writer.toString();
    System.out.println(s);
    //assertTrue(s,s.endsWith(suppressAnyEmpty));
  }

  public void testNoAttributeMatches() throws Exception {
    String xmlString = "<doc id=\"\"><name>John</name><age/><sex/></doc>";
    StreamSource streamSource = new StringStreamSource(xmlString);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "*";
    String attributeNames = "foo";
    NameTest elementNameTest = NameTest.parse(nameContext, elementNames);
    NameTest attributeNameTest = NameTest.parse(nameContext, attributeNames);
    XMLFilter xmlFilter = new RemoveEmptyAttributes(elementNameTest, NameTest.NONE, attributeNameTest, NameTest.NONE);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String s = writer.toString();
    System.out.println(s);
    //assertTrue(s,s.endsWith(suppressAnyEmpty));
  }
}                    


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

import java.io.InputStream;
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

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.NameTest;
import com.servingxml.util.QualifiedName;
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

public class RemoveEmptyElementsTest extends TestCase {

  private static final String suppressNone = "<doc><name>John</name><age/><sex/></doc>";
  private static final String suppressAnyEmpty = "<doc><name>John</name></doc>";
  private static final String suppressEmptyAge = "<doc><name>John</name><sex/></doc>";
  private static final String suppressEmptyAgeAndSex = "<doc><name>John</name></doc>";
  private SimpleQnameContext nameContext;

  public RemoveEmptyElementsTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping("ns1","http://www.namespace.com");
    nameContext = new SimpleQnameContext(prefixMap);
  }

  public void testSuppressNone() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("doc"),0);
    recordBuilder.setString(new QualifiedName("name"),"John");
    recordBuilder.setString(new QualifiedName("age"),"");
    recordBuilder.setString(new QualifiedName("sex"),"");
    Record record = recordBuilder.toRecord();
    XMLReader xmlReader = record.createXmlReader(nameContext.getPrefixMap());
    Source source = new SAXSource(xmlReader,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();


    Result result = new StreamResult(writer);
    transformer.transform(source,result);

    String s = writer.toString();
    assertTrue(s,s.endsWith(suppressNone));
  }

  public void testSuppressEmpty() throws Exception {
    String xmlString = "<doc><name>John</name><age/><sex/></doc>";
    StreamSource streamSource = new StringStreamSource(xmlString);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "*";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String s = writer.toString();
    assertTrue(s,s.endsWith(suppressAnyEmpty));
  }

  public void testSuppressNone2() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("doc"),0);
    recordBuilder.setString(new QualifiedName("name"),"John");
    recordBuilder.setString(new QualifiedName("age"),"");
    recordBuilder.setString(new QualifiedName("sex"),"");
    Record record = recordBuilder.toRecord();
    XMLReader xmlReader = record.createXmlReader(nameContext.getPrefixMap());

    String elementNames = "name";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String s = writer.toString();
    assertTrue(s,!s.endsWith(suppressAnyEmpty));
    assertTrue(s,s.endsWith(suppressNone));
  }

  public void testSuppressEmptyAge() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("doc"),0);
    recordBuilder.setString(new QualifiedName("name"),"John");
    recordBuilder.setString(new QualifiedName("age"),"");
    recordBuilder.setString(new QualifiedName("sex"),"");
    Record record = recordBuilder.toRecord();
    XMLReader xmlReader = record.createXmlReader(nameContext.getPrefixMap());

    String elementNames = "age";

    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);

    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String s = writer.toString();
    assertTrue(s,!s.endsWith(suppressAnyEmpty));
    assertTrue(s,s.endsWith(suppressEmptyAge));
  }

  public void testSuppressEmptyAgeAndSex() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("doc"),0);
    recordBuilder.setString(new QualifiedName("name"),"John");
    recordBuilder.setString(new QualifiedName("age"),"");
    recordBuilder.setString(new QualifiedName("sex"),"");
    Record record = recordBuilder.toRecord();
    XMLReader xmlReader = record.createXmlReader(nameContext.getPrefixMap());

    String elementNames = "age sex";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String s = writer.toString();
    assertTrue(s,s.endsWith(suppressEmptyAgeAndSex));
  }

  public void testRemoveEmptyDescendentsTrue() throws Exception {
    String expected = "<message/>";
    String input = "<message><billAddress><houseNumber/><street/></billAddress></message>";

    StreamSource streamSource = new StringStreamSource(input);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "billAddress";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration,true);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String output = writer.toString();

    assertTrue(output + "=" + expected,output.endsWith(expected));
  }

  public void testRemoveEmptyDescendentsTrue2() throws Exception {
    String expected = "<message><billAddress><houseNumber>123</houseNumber><street/></billAddress></message>";
    String input = "<message><billAddress><houseNumber>123</houseNumber><street/></billAddress></message>";

    StreamSource streamSource = new StringStreamSource(input);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "billAddress";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration,true);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String output = writer.toString();

    assertTrue(output + "=" + expected,output.endsWith(expected));
  }

  public void testRemoveEmptyDescendentsFalse() throws Exception {
    String expected = "<message/>";
    String input = "<message><billAddress><houseNumber/><street/></billAddress></message>";

    StreamSource streamSource = new StringStreamSource(input);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "billAddress";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration,false);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String output = writer.toString();

    assertTrue(output + "!=" + expected,!output.endsWith(expected));
  }

  public void testRemoveEmptyElements() throws Exception {
    InputStream is = getClass().getResourceAsStream( "/remove-empty-element-filter.xml" );
    StreamSource streamSource = new InputStreamSourceAdaptor(is);
    SaxSource saxSource = new StreamSourceSaxSource(streamSource);
    XMLReader xmlReader = saxSource.createXmlReader();

    String elementNames = "ns1:Class ns1:Description ns1:Rate ns1:Percent ns1:Wages ns1:Tariff ns1:BasicTariffPremium";
    NameTest elementEnumeration = NameTest.parse(nameContext, elementNames);
    XMLFilter xmlFilter = new RemoveEmptyElements(elementEnumeration,false);
    xmlFilter.setParent(xmlReader);
    Source source = new SAXSource(xmlFilter,new InputSource());

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    String output = writer.toString();
    //System.out.println(output);
  }
}                    


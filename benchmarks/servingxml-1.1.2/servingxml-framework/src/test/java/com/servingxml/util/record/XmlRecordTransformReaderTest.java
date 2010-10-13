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

package com.servingxml.util.record;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.w3c.dom.Element;

import junit.framework.TestCase;

import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.system.Logger;
import com.servingxml.util.xml.DefaultMatchable;
import com.servingxml.util.xml.Matchable;
import com.servingxml.util.xml.MatchableImpl;
import com.servingxml.util.xml.RepeatingGroupMatchable;
import com.servingxml.util.xml.Selectable;
import com.servingxml.util.xml.SelectableImpl;
import com.servingxml.util.xml.XmlRecordTransformReader;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

public class XmlRecordTransformReaderTest extends TestCase {

  private final SimpleQnameContext nameContext = new SimpleQnameContext();

  private NameTableImpl nameTable = new NameTableImpl();

  public XmlRecordTransformReaderTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
  }

  public void testNestedSegment() throws Exception {
    String document = "<rootNode><name>root</name><childNode><name>Entity.LONDON</name><childNode><name>BookName.EQUITIES_NY</name></childNode><childNode><name>BookName.EQUITIES_NY</name></childNode></childNode></rootNode>";
    //System.out.println(document);
    String[] parameterQnames = new String[0];

    StringReader documentReader = new StringReader(document);

    final SimpleQnameContext context = new SimpleQnameContext(nameTable);

    Name recordTypeName = new QualifiedName("rootNode");
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping(SystemConstants.SERVINGXMLX_NS_PREFIX,SystemConstants.SERVINGXMLX_NS_URI);

    //  Set up matchable array
    Name segmentTypeName = new QualifiedName("childNodeRecord");
    Name fieldName = new QualifiedName("name");
    Name childNodeName = new QualifiedName("childNode");
    Matchable matchable1 = new MatchableImpl("field.1",fieldName,"name");
    Matchable matchable2 = new MatchableImpl("field.2",fieldName,"name");

    Matchable[] matchables1 = new Matchable[]{matchable2};
    Matchable matchable3 = new RepeatingGroupMatchable("field.3", "childNode", childNodeName, "/*", segmentTypeName, matchables1);
    Matchable[] matchables2 = new Matchable[]{matchable1,matchable3};
    Matchable matchable4 = new RepeatingGroupMatchable("field.4", "childNode", childNodeName, "/*", segmentTypeName, matchables2);
    Matchable[] matchables3 = new Matchable[]{matchable1,matchable4};
    Matchable matchable5 = new RepeatingGroupMatchable("field.5", "childNode", childNodeName, "/*", segmentTypeName, matchables3);

    Matchable[] fieldMaps = new Matchable[]{matchable1,matchable5};
    XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, "/*",
      fieldMaps,prefixMap,"2.0",parameterQnames);
    Source styleSource = new SAXSource(reader,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer styleTransformer = transformerFactory.newTransformer();
    StringWriter styleWriter = new StringWriter();
    Result styleResult = new StreamResult(styleWriter);
    styleTransformer.transform(styleSource,styleResult);
    //System.out.println (styleWriter.toString());

    Templates templates = transformerFactory.newTemplates(styleSource);
    Transformer transformer = templates.newTransformer();
    //StringWriter writer = new StringWriter();
    //Result result = new StreamResult(writer);
    //System.out.println(writer.toString());

    //  Set up receiver

    //  Create record
    StreamSource docSource = new StreamSource(documentReader);
    Transformer docTransformer = transformerFactory.newTransformer();
    StringWriter docWriter = new StringWriter();
    Result docResult = new StreamResult(docWriter);
    //docTransformer.transform(docSource,docResult);
    //System.out.println (docWriter.toString());

    RecordReceiver recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        //System.out.println ("Output Record = \n" + record.toXmlString(context));
      }
    };

    RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
    SAXResult saxResult = new SAXResult(receiver);
    transformer.transform(docSource,saxResult);
  }

  public void atestSegment() throws Exception {
    String document = "<all><field1>val1</field1><field2>val2</field2><field3>val3</field3><compositeA attr1=\"av11\" attr2=\"av21\" attr3=\"av31\"/> <compositeA attr1=\"av21\" attr2=\"av22\" attr3=\"av23\"/><compositeA attr1=\"av31\" attr2=\"av32\" attr3=\"av33\"/><compositeB attrx=\"avx\" attry=\"avy\"/></all>";
    //System.out.println(document);
    String[] parameterQnames = new String[0];

    StringReader documentReader = new StringReader(document);

    final SimpleQnameContext context = new SimpleQnameContext(nameTable);

    Name recordTypeName = new QualifiedName("all");
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping(SystemConstants.SERVINGXMLX_NS_PREFIX,SystemConstants.SERVINGXMLX_NS_URI);

    //  Set up matchable array
    Name field1Name = new QualifiedName("f1");
    Name field2Name = new QualifiedName("f2");
    Name field3Name = new QualifiedName("f3");
    Name field4Name = new QualifiedName("compA");
    Name recordType4Name = new QualifiedName("compARecord");
    Name field5Name = new QualifiedName("compB");
    Name recordType5Name = new QualifiedName("compBRecord");
    Matchable matchable1 = new MatchableImpl("field.1",field1Name,"field1");
    Matchable matchable2 = new MatchableImpl("field.2",field2Name,"field2");
    Matchable matchable3 = new MatchableImpl("field.3",field3Name,"field3");

    Name field4aName = new QualifiedName("ca1");
    Matchable matchable4a = new MatchableImpl("field.4", field4aName, "@attr1");
    Name field4bName = new QualifiedName("ca2");
    Matchable matchable4b = new MatchableImpl("field.5", field4bName, "@attr2");
    Name field4cName = new QualifiedName("ca3");
    Matchable matchable4c = new MatchableImpl("field.6", field4cName, "@attr3");
    Matchable[] matchables4 = new Matchable[]{matchable4a,matchable4b,matchable4c};

    Name field5aName = new QualifiedName("cb1");
    Matchable matchable5a = new MatchableImpl("field.7", field5aName, "@attrx");
    Name field5bName = new QualifiedName("cb2");
    Matchable matchable5b = new MatchableImpl("field.8", field5bName, "@attry");
    Matchable[] matchables5 = new Matchable[]{matchable5a, matchable5b};

    Matchable matchable4 = new RepeatingGroupMatchable("field.9", "compositeA", field4Name, "/*", recordType4Name, matchables4);
    Matchable matchable5 = new RepeatingGroupMatchable("field.10", "compositeB", field5Name, "/*", recordType5Name, matchables5);

    Matchable[] fieldMaps = new Matchable[]{matchable1,matchable2,matchable3,matchable4,matchable5};
    XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, "/*",
      fieldMaps,prefixMap,"2.0",parameterQnames);
    Source styleSource = new SAXSource(reader,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer styleTransformer = transformerFactory.newTransformer();
    StringWriter styleWriter = new StringWriter();
    Result styleResult = new StreamResult(styleWriter);
    styleTransformer.transform(styleSource,styleResult);
    //System.out.println (styleWriter.toString());

    Templates templates = transformerFactory.newTemplates(styleSource);
    Transformer transformer = templates.newTransformer();
    //StringWriter writer = new StringWriter();
    //Result result = new StreamResult(writer);
    //System.out.println(writer.toString());

    //  Set up receiver

    //  Create record
    StreamSource docSource = new StreamSource(documentReader);
    Transformer docTransformer = transformerFactory.newTransformer();
    StringWriter docWriter = new StringWriter();
    Result docResult = new StreamResult(docWriter);
    //docTransformer.transform(docSource,docResult);
    //System.out.println (docWriter.toString());

    RecordReceiver recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        //System.out.println ("Output Record = \n" + record.toXmlString(context));
      }
    };

    RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
    SAXResult saxResult = new SAXResult(receiver);
    transformer.transform(docSource,saxResult);
  }
  
  public void atestFragmentTemplatesXmlReader() 
  throws Exception {

    String[] parameterQnames = new String[0];
    final SimpleQnameContext context = new SimpleQnameContext(nameTable);
    Name recordTypeName = new QualifiedName(SystemConstants.SERVINGXMLX_NS_URI,"books");
    //Name recordTypeName = new QualifiedName("books");

    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping(SystemConstants.SERVINGXMLX_NS_PREFIX,SystemConstants.SERVINGXMLX_NS_URI);

    Name titleName = new QualifiedName(SystemConstants.SERVINGXMLX_NS_URI,"title");
    Name authorName = new QualifiedName("author");
    Selectable selectable1 = new SelectableImpl(titleName,"title");
    Matchable titleFieldMap = new DefaultMatchable("field.1", new Selectable[]{selectable1});
    Selectable selectable2 = new SelectableImpl(authorName,"author");
    Matchable authorFieldMap = new DefaultMatchable("field.2", new Selectable[]{selectable2});

    Matchable[] fieldMaps = new Matchable[]{titleFieldMap,authorFieldMap};
    XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, "/*",
      fieldMaps,prefixMap,"1.0", parameterQnames);
    Source styleSource = new SAXSource(reader,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer styleTransformer = transformerFactory.newTransformer();
    StringWriter styleWriter = new StringWriter();
    Result styleResult = new StreamResult(styleWriter);
    styleTransformer.transform(styleSource,styleResult);
    //System.out.println (styleWriter.toString());

    RecordBuilder fRecordBuilder = new RecordBuilder(new QualifiedName("books"));
    fRecordBuilder.setString(new QualifiedName("title"),"Pulp");
    fRecordBuilder.setStringArray(new QualifiedName("author"),new String[]{"Bukowski","Milton"});
    Record fragment = fRecordBuilder.toRecord();

    SAXSource docSource = new SAXSource(fragment.createXmlReader(context.getPrefixMap()),new InputSource());
    Transformer docTransformer = transformerFactory.newTransformer();
    StringWriter docWriter = new StringWriter();
    Result docResult = new StreamResult(docWriter);
    docTransformer.transform(docSource,docResult);
    //System.out.println (docWriter.toString());

    Templates templates = transformerFactory.newTemplates(styleSource);
    Transformer transformer = templates.newTransformer();
    //StringWriter writer = new StringWriter();
    //Result result = new StreamResult(writer);
    //System.out.println(writer.toString());

    RecordReceiver recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        //System.out.println ("Output Record = \n" + record.toXmlString(context));
      }
    };

    RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
    SAXResult saxResult = new SAXResult(receiver);
    transformer.transform(docSource,saxResult);
  }

  public void atestFragmentTemplatesXmlReader2() 
  throws Exception {

    String[] parameterQnames = new String[0];
    final SimpleQnameContext context = new SimpleQnameContext(nameTable);
    String xmlString = "<Swath><Shape>Polygon</Shape><GeoPoint><lat>1</lat><lon>2</lon></GeoPoint><GeoPoint><lat>3</lat><lon>4</lon></GeoPoint><GeoPoint><lat>5</lat><lon>6</lon></GeoPoint></Swath>";
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    builderFactory.setNamespaceAware(true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(xmlString));
    Document doc = builder.parse(inputSource);
    Element root = doc.getDocumentElement();
    DOMSource docSource = new DOMSource(root);

    Name recordTypeName = new QualifiedName(SystemConstants.SERVINGXMLX_NS_URI,"book");
    //Name recordTypeName = new QualifiedName("GeoPoint");
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping(SystemConstants.SERVINGXMLX_NS_PREFIX,SystemConstants.SERVINGXMLX_NS_URI);

    Name latName = new QualifiedName("lat");
    Name lonName = new QualifiedName("lon");
    Selectable selectable1 = new SelectableImpl(latName,"lat");
    Matchable latFieldMap = new DefaultMatchable("field.1", new Selectable[]{selectable1});
    Selectable selectable2 = new SelectableImpl(lonName,"lon");
    Matchable lonFieldMap = new DefaultMatchable("field.2", new Selectable[]{selectable2});

    Matchable[] fieldMaps = new Matchable[]{latFieldMap,lonFieldMap};
    XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, "/Swath/GeoPoint",
      fieldMaps,prefixMap,"1.0", parameterQnames);
    Source styleSource = new SAXSource(reader,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer styleTransformer = transformerFactory.newTransformer();
    StringWriter styleWriter = new StringWriter();
    Result styleResult = new StreamResult(styleWriter);
    styleTransformer.transform(styleSource,styleResult);
    //System.out.println (styleWriter.toString());

    RecordBuilder fRecordBuilder = new RecordBuilder(recordTypeName);
    fRecordBuilder.setString(new QualifiedName("lat"),"1");
    fRecordBuilder.setString(new QualifiedName("lon"),"2");
    Record fragment = fRecordBuilder.toRecord();

    //SAXSource docSource = new SAXSource(fragment.createXmlReader(),new InputSource());
    Transformer docTransformer = transformerFactory.newTransformer();
    StringWriter docWriter = new StringWriter();
    Result docResult = new StreamResult(docWriter);
    docTransformer.transform(docSource,docResult);
    //System.out.println (docWriter.toString());

    Templates templates = transformerFactory.newTemplates(styleSource);
    Transformer transformer = templates.newTransformer();
    //StringWriter writer = new StringWriter();
    //Result result = new StreamResult(writer);
    //System.out.println(writer.toString());


    RecordReceiver recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        //System.out.println ("Output Record = \n" + record.toXmlString(context));
      }
    };

    RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
    SAXResult saxResult = new SAXResult(receiver);
    transformer.transform(docSource,saxResult);
  }

  public void atestFragmentTemplatesXmlReader3() 
  throws Exception {

    String[] parameterQnames = new String[0];
    final SimpleQnameContext context = new SimpleQnameContext(nameTable);
    String xmlString = "<Swath><Shape>Polygon</Shape><GeoPoint><lat>1</lat><lon>2</lon></GeoPoint><GeoPoint><lat>3</lat><lon>4</lon></GeoPoint><GeoPoint><lat>5</lat><lon>6</lon></GeoPoint></Swath>";
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    builderFactory.setNamespaceAware(true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(xmlString));
    Document doc = builder.parse(inputSource);
    Element root = doc.getDocumentElement();
    DOMSource docSource = new DOMSource(root);

    Name recordTypeName = new QualifiedName(SystemConstants.SERVINGXMLX_NS_URI,"book");
    //Name recordTypeName = new QualifiedName("GeoPoint");
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping(SystemConstants.SERVINGXMLX_NS_PREFIX,SystemConstants.SERVINGXMLX_NS_URI);

    Name latName = new QualifiedName("lat");
    Name lonName = new QualifiedName("lon");
    Selectable selectable1 = new SelectableImpl(latName,"lat");
    Matchable latFieldMap = new DefaultMatchable("field.1", "GeoPoint",new Selectable[]{selectable1});
    Selectable selectable2 = new SelectableImpl(lonName,"lon");
    Matchable lonFieldMap = new DefaultMatchable("field.2", "GeoPoint",new Selectable[]{selectable2});

    Matchable[] fieldMaps = new Matchable[]{latFieldMap,lonFieldMap};
    XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, "/*",
      fieldMaps,prefixMap,"1.0", parameterQnames);
    Source styleSource = new SAXSource(reader,new InputSource());
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer styleTransformer = transformerFactory.newTransformer();
    StringWriter styleWriter = new StringWriter();
    Result styleResult = new StreamResult(styleWriter);
    styleTransformer.transform(styleSource,styleResult);
    //System.out.println (styleWriter.toString());

    RecordBuilder fRecordBuilder = new RecordBuilder(recordTypeName);
    fRecordBuilder.setString(new QualifiedName("lat"),"1");
    fRecordBuilder.setString(new QualifiedName("lon"),"2");
    Record fragment = fRecordBuilder.toRecord();

    //SAXSource docSource = new SAXSource(fragment.createXmlReader(),new InputSource());
    Transformer docTransformer = transformerFactory.newTransformer();
    StringWriter docWriter = new StringWriter();
    Result docResult = new StreamResult(docWriter);
    docTransformer.transform(docSource,docResult);
    //System.out.println (docWriter.toString());

    Templates templates = transformerFactory.newTemplates(styleSource);
    Transformer transformer = templates.newTransformer();
    //StringWriter writer = new StringWriter();
    //Result result = new StreamResult(writer);
    //System.out.println(writer.toString());


    RecordReceiver recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        //System.out.println ("Output Record = \n" + record.toXmlString(context));
      }
    };

    RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
    SAXResult saxResult = new SAXResult(receiver);
    transformer.transform(docSource,saxResult);
  }
}

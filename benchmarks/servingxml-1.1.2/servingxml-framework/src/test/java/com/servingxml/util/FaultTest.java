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

import java.io.Writer;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.servingxml.util.ServingXmlFault;
import com.servingxml.util.ServingXmlFaultDetail;
import com.servingxml.util.ServingXmlFaultReader;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FaultTest extends TestCase {
  public FaultTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testXMLReader() throws Exception {
    ServingXmlFaultDetail detail = new ServingXmlFaultDetail();
    detail.addEntry(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"message"),
                    "Big Error");

    Name[] subcodes = new Name[0];
    XMLReader xmlReader = new ServingXmlFaultReader(ServingXmlFaultCodes.RECEIVER_CODE,subcodes,"no book",detail);
    InputSource inputSource = new InputSource();
    Source xmlSource = new SAXSource(xmlReader,inputSource);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(xmlSource,result);
    String s = writer.toString();
    //System.out.println (s);
  }

  public void testXMLReaderFromFault() throws Exception {
    List<ServingXmlFaultDetail.Entry> detailList = new ArrayList<ServingXmlFaultDetail.Entry>();
    detailList.add(new ServingXmlFaultDetail.Entry(
      new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"message"),
      "Big Error"));

    ServingXmlFaultDetail detail = new ServingXmlFaultDetail();
    detail.addEntry(new QualifiedName("name"),"Name is required");

    ServingXmlFault fault = new ServingXmlFault(ServingXmlFaultCodes.RECEIVER_CODE,
      "Please correct errors and submit again.", 
      detail);

    XMLReader xmlReader = fault.createXmlReader();
    InputSource inputSource = new InputSource();
    Source xmlSource = new SAXSource(xmlReader,inputSource);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Writer writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(xmlSource,result);
    String s = writer.toString();
    //System.out.println (s);
  }
}



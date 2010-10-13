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

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import com.servingxml.util.Name;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;
import com.servingxml.app.ServiceContext;

public class XsltChooseReaderTest extends TestCase {
  private static final Name capName = new QualifiedName("cap");
  private static final Name floorName = new QualifiedName("floor");
  private static final Name swapFloatingLegName = new QualifiedName("swapFloatingLeg");
  private static final Name swapFixedLegName = new QualifiedName("swapFixedLeg");
  private static final Name defaultName = new QualifiedName("default");

  private final SimpleQnameContext nameContext = new SimpleQnameContext();

  public XsltChooseReaderTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
  }

  public void testCap() 
  throws Exception {
    Name recordTypeName = performTest("CAP","");
    assertTrue(recordTypeName.equals(capName));
  }

  public void testFloor() 
  throws Exception {
    Name recordTypeName = performTest("FLOOR","");
    assertTrue(recordTypeName.equals(floorName));
  }

  public void testSwapFloatingLeg() 
  throws Exception {
    Name recordTypeName = performTest("SWAP","FLOAT");
    assertTrue(recordTypeName.equals(swapFloatingLegName));
  }

  public void testSwapFixedLeg() 
  throws Exception {
    Name recordTypeName = performTest("SWAP","FIX");
    assertTrue(recordTypeName.equals(swapFixedLegName));
  }

  public void testDefault() 
  throws Exception {
    Name recordTypeName = performTest("TRS","FLOAT");
    assertTrue(recordTypeName.equals(defaultName));
  }

  public Name performTest(String recordType, String style) throws Exception {
    PrefixMapImpl prefixMap = new PrefixMapImpl();
    prefixMap.setPrefixMapping(SystemConstants.SERVINGXMLX_NS_PREFIX,SystemConstants.SERVINGXMLX_NS_URI);

    XsltTestNameMap capTestable = new XsltTestNameMap("recordType='CAP'",capName);
    XsltTestNameMap floorTestable = new XsltTestNameMap("recordType='FLOOR'",floorName);
    XsltTestNameMap swapFloatingLegTestable = new XsltTestNameMap("recordType='SWAP' and style='FLOAT'",swapFloatingLegName);
    XsltTestNameMap swapFixedLegTestable = new XsltTestNameMap("recordType='SWAP' and style='FIX'",swapFixedLegName);
    XsltTestNameMap defaultTestable = new XsltTestNameMap("",defaultName);

    XsltTestNameMap[] testables = new XsltTestNameMap[]{capTestable,floorTestable,swapFloatingLegTestable,swapFixedLegTestable,defaultTestable};

    String[] tests = {
      capTestable.getTestExpression(),
      floorTestable.getTestExpression(),
      swapFloatingLegTestable.getTestExpression(),
      swapFixedLegTestable.getTestExpression(),
      defaultTestable.getTestExpression()
    };

    TransformerFactory transformerFactory = TransformerFactory.newInstance();

    String baseUri = "";
    XsltChooserFactory chooserFactory = new XsltChooserFactory(transformerFactory, baseUri, tests, prefixMap, "2.0");
    XsltChooser chooser = chooserFactory.createXsltChooser();

    Name recordIdName = new QualifiedName(SystemConstants.SERVINGXMLX_NS_URI,"tag");
    Name recordTypeName = new QualifiedName("recordType");
    Name styleName = new QualifiedName("style");
    RecordBuilder recordBuilder = new RecordBuilder(recordIdName);
    recordBuilder.setString(recordTypeName,recordType);
    recordBuilder.setString(styleName,style);
    Record record = recordBuilder.toRecord();

    SAXSource docSource = new SAXSource(record.createXmlReader(nameContext.getPrefixMap()),new InputSource());
    //Transformer docTransformer = transformerFactory.newTransformer();
    //StringWriter docWriter = new StringWriter();
    //Result docResult = new StreamResult(docWriter);
    //docTransformer.transform(docSource,docResult);
    //System.out.println(docWriter.toString());

    Name testResult = Name.EMPTY;
    int index = chooser.choose(docSource, Record.EMPTY);
    if (index >= 0 && index < testables.length) {
      testResult = testables[index].getName();
    }

    //System.out.println("result = " + testResult);

    return testResult;
  }

  public static class XsltTestNameMap implements Testable {

    private final String testExpr;
    private final Name name;

    public XsltTestNameMap(String testExpr, Name name) {
      this.testExpr = testExpr;
      this.name = name;
    }

    public Name getName() {                
      return name;
    }

    public String getTestExpression() {
      return testExpr;
    }
  }
}



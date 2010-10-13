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

package com.servingxml.components.choose;

import javax.xml.transform.TransformerFactory;

import org.xml.sax.helpers.AttributesImpl;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordmapping.MapXml;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.app.Flow;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QnameContext;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.components.recordmapping.GroupState;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ChooseRecordMappingTest extends TestCase {

  private MutableNameTable nameTable = new NameTableImpl();

  public ChooseRecordMappingTest(String name) {
    super(name);
  }
  protected void setUp() {
  }
  public void testChooseRecordMapping() throws Exception {
/*
    QnameContext nameContext = new SimpleQnameContext(nameTable);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    XPathBooleanExpressionFactory exprFactory = new XPathBooleanExpressionFactory(nameContext,
      "1=1", "2.0", transformerFactory);
    XPathBooleanExpression booleanExpr = exprFactory.createXPathBooleanExpression();

    MapXmlFactory recordMapFactory = new RecordMapFactoryImpl();
    Alternative alternative = new WhenAlternative(booleanExpr, recordMapFactory);

    MapXml recordMap = recordMapFactory.createMapXml(context);
    MapXml[] recordMaps = new MapXml[]{recordMap};

    Alternative[] alternatives = new Alternative[]{alternative};
    AlternativeSimpleMapXmlContainer chooseRecordMap = new AlternativeSimpleMapXmlContainer(alternatives, recordMaps);
*/    
  }

  static class RecordMapFactoryImpl implements MapXmlFactory {
    public MapXml createMapXml(ServiceContext context) {
      return new RecordMapImpl();
    }
    public boolean isGroup() {
      return false;
    }
    public boolean isRecord() {
      return false;
    }
    public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
    }
  }

  static class RecordMapImpl implements MapXml {
    public void addToAttributes(ServiceContext context, Flow flow, 
      Record variables, AttributesImpl attributes) {
    }

    public void writeRecord(ServiceContext context, Flow flow, 
      Record previousRecord, Record nextRecord, ExtendedContentHandler handler, 
      GroupState groupListener) {
    }

    public void groupStarted(ServiceContext context, Flow flow, 
      Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
    }

    public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    }

    public boolean isGrouping() {
      return false;
    }
  }

  public void flush() {
  }
  
}                    


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

package com.servingxml.components.recordio;


import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;
import com.servingxml.components.xsltconfig.XsltConfiguration;

/**
 * A command for mapping a record in a flat file into an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                                                             
                                                            
public class RecordAccepterFactory {
  private final Name recordTypeName;
  private final XPathBooleanExpressionFactory testExprFactory;
  private final QnameContext context;

  public static RecordAccepterFactory newInstance() {
    return new RecordAccepterFactory();
  }

  public static RecordAccepterFactory newInstance(Name recordTypeName) {
    return new RecordAccepterFactory(recordTypeName);
  }

  public static RecordAccepterFactory newInstance(QnameContext context, 
    XsltConfiguration xsltConfiguration, String test) {
    XPathBooleanExpressionFactory testExprFactory = new XPathBooleanExpressionFactoryImpl(
        context, test, xsltConfiguration.getVersion(),"",
      xsltConfiguration.getTransformerFactory());
    return new RecordAccepterFactory(context, testExprFactory);
  }

  private RecordAccepterFactory() {
    this.recordTypeName = Name.EMPTY;
    this.testExprFactory = null;
    this.context = null;
  }

  private RecordAccepterFactory(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
    this.testExprFactory = null;
    this.context = null;
  }

  private RecordAccepterFactory(QnameContext context, XPathBooleanExpressionFactory testExprFactory) {
    this.context = context;
    this.recordTypeName = Name.EMPTY;
    this.testExprFactory = testExprFactory;
  }

  public RecordAccepter createRecordAccepter() {
    RecordAccepter accepter = RecordAccepter.ALL;
    if (!recordTypeName.isEmpty()) {
      accepter = new RecordTypeRecordAccepter(recordTypeName);
    } else if (testExprFactory != null) {
      XPathBooleanExpression testExpr = testExprFactory.createXPathBooleanExpression();
      accepter = new XPathRecordAccepter(context.getPrefixMap(),testExpr);
    }
    return accepter;
  }
}



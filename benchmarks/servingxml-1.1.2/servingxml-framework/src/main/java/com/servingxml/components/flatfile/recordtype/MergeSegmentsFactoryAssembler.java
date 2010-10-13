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

package com.servingxml.components.flatfile.recordtype;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.components.recordmapping.GroupRecognizerImpl;
import com.servingxml.util.Name;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;
import com.servingxml.util.xml.SimpleXPathBooleanExpressionFactory;
import com.servingxml.app.Environment;

//  Revisit

/**
 * The <code>FlatRecordTypeFactoryAssembler</code> implements an assembler for
 * assembling flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MergeSegmentsFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String startTest = "";
  private String endTest = "";
  private VbsFlatRecordTypeFactory sdwRecordTypeFactory = null;
  private FlatRecordTypeFactory dataRecordTypeFactory = null;
  private String segmentLength = null;
  private boolean suppressRDW = false;

  public void setSegmentLength(String segmentLength) {
    this.segmentLength = segmentLength;
  }

  public void setSuppressRDW(boolean suppressRDW) {
    this.suppressRDW = suppressRDW;
  }

  public void setStartTest(String startTest) {
    this.startTest = startTest;
  }

  public void setEndTest(String endTest) {
    this.endTest = endTest;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(VbsFlatRecordTypeFactory sdwRecordTypeFactory) {

    this.sdwRecordTypeFactory = sdwRecordTypeFactory;
  }

  public void injectComponent(FlatRecordTypeFactory dataRecordTypeFactory) {

    this.dataRecordTypeFactory = dataRecordTypeFactory;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public RecordCombinationFactory assemble(ConfigurationContext context) {

    Environment env = new Environment(parameterDescriptors,context.getQnameContext());

    if (dataRecordTypeFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:flatRecordType");
      throw new ServingXmlException(message);
    }

    String baseUri = context.getQnameContext().getBase();
    XPathBooleanExpressionFactory startTestFactory;
    if (startTest.length() > 0) {
      startTestFactory = new XPathBooleanExpressionFactoryImpl(
        context.getQnameContext(), startTest, xsltConfiguration.getVersion(),
      baseUri, xsltConfiguration.getTransformerFactory());
    } else {
      startTestFactory = new SimpleXPathBooleanExpressionFactory(XPathBooleanExpression.ALWAYS_TRUE);
    }
    XPathBooleanExpressionFactory endTestFactory;
    if (endTest.length() > 0) {
      endTestFactory = new XPathBooleanExpressionFactoryImpl(
        context.getQnameContext(), endTest, xsltConfiguration.getVersion(),
      baseUri, xsltConfiguration.getTransformerFactory());
    } else {
      endTestFactory = startTestFactory;
    }

    GroupRecognizer groupRecognizer = new GroupRecognizerImpl(env.getQnameContext().getPrefixMap(), startTestFactory, endTestFactory);

    IntegerSubstitutionExpr segmentLengthExpr = segmentLength == null? IntegerSubstitutionExpr.NULL : 
      IntegerSubstitutionExpr.parseInt(context.getQnameContext(), segmentLength);

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    RecordCombinationFactory recordCombinationFactory = new MergeSegmentsFactory(groupRecognizer, 
                                                                              dataRecordTypeFactory,
                                                                              segmentLengthExpr,
                                                                              suppressRDW,
                                                                              flatFileOptionsFactory);
    //if (parameterDescriptors.length > 0) {
    //  recordCombinationFactory = new FlatRecordTypeFactoryPrefilter(recordCombinationFactory,
    //                                                         parameterDescriptors);
    //}
    return recordCombinationFactory;
  }
}

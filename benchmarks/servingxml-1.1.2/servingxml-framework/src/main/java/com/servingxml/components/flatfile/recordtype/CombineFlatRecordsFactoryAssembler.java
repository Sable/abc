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
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;
import com.servingxml.util.xml.SimpleXPathBooleanExpressionFactory;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.app.Environment;

//  Revisit

/**
 * The <code>FlatRecordTypeFactoryAssembler</code> implements an assembler for
 * assembling flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CombineFlatRecordsFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String startTest = "";
  private String endTest = "";
  private VbsFlatRecordTypeFactory sdwRecordTypeFactory = null;
  private FlatRecordTypeFactory dataRecordTypeFactory = null;
  private Name repeatingGroupFieldName = Name.EMPTY;
  private String recordTypeName="";   

  public void setRecordType(String recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setRepeatingGroup(Name repeatingGroupFieldName) {
    this.repeatingGroupFieldName = repeatingGroupFieldName;
  }

  public void setField(Name repeatingGroupFieldName) {
    this.repeatingGroupFieldName = repeatingGroupFieldName;
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

    if (recordTypeName.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "recordType");
      throw new ServingXmlException(message);
    }

    NameSubstitutionExpr recordTypeNameExpr = NameSubstitutionExpr.parse(context.getQnameContext(),recordTypeName);

    if (dataRecordTypeFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:flatRecordType");
      throw new ServingXmlException(message);
    }

    if (repeatingGroupFieldName.isEmpty()) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                             context.getElement().getTagName(),
                                                             "repeatingGroup");
      throw new ServingXmlException(msg);
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

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    RecordCombinationFactory recordCompositionFactory = new CombineFlatRecordsFactory(recordTypeNameExpr,
                                                                                 repeatingGroupFieldName,
                                                                                     groupRecognizer, 
                                                                                     dataRecordTypeFactory,
                                                                                     flatFileOptionsFactory);
    //if (parameterDescriptors.length > 0) {
    //  recordCompositionFactory = new FlatRecordTypeFactoryPrefilter(recordCompositionFactory,
    //                                                         parameterDescriptors);
    //}
    return recordCompositionFactory;
  }
}

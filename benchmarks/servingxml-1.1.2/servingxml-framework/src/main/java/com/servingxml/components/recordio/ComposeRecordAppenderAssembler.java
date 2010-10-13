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

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Environment;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.components.recordmapping.GroupRecognizerImpl;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.SimpleXPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class ComposeRecordAppenderAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String startTest = "";
  private String endTest = "";
  private String recordTypeName = "";
  private Name repeatingGroupName = Name.EMPTY;
  private NewField[] newFields = NewField.EMPTY_ARRAY;

  public void setStartTest(String startTest) {
    this.startTest = startTest;
  }

  public void setEndTest(String endTest) {
    this.endTest = endTest;
  }

  public void setRecordType(String recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setCompositeRecordType(String recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setField(Name repeatingGroupName) {
    this.repeatingGroupName = repeatingGroupName;
  }

  public void setRepeatingGroupField(Name repeatingGroupName) {
    this.repeatingGroupName = repeatingGroupName;
  }

  public void setRepeatingGroup(Name repeatingGroupName) {
    this.repeatingGroupName = repeatingGroupName;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(NewField[] newFields) {
    this.newFields = newFields;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    Environment env = new Environment(parameterDescriptors,context.getQnameContext());
    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (recordTypeName.length() == 0) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                             context.getElement().getTagName(),
                                                             "recordType");
      throw new ServingXmlException(msg);
    }

    if (repeatingGroupName.isEmpty()) {
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

    NameSubstitutionExpr recordTypeEvaluator = NameSubstitutionExpr.parse(context.getQnameContext(),recordTypeName);
    RecordFilterAppender recordFilterAppender = new ComposeRecordAppender(recordTypeEvaluator, repeatingGroupName,
      groupRecognizer, newFields);

    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class ComposeRecordAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender {
  private final NameSubstitutionExpr recordTypeEvaluator;
  private final Name repeatingGroupName;
  private final GroupRecognizer groupRecognizer;
  private final NewField[] newFields;
  
  public ComposeRecordAppender(NameSubstitutionExpr recordTypeEvaluator, Name repeatingGroupName, 
    GroupRecognizer groupRecognizer, NewField[] newFields) {
    this.recordTypeEvaluator = recordTypeEvaluator;
    this.repeatingGroupName = repeatingGroupName;
    this.groupRecognizer = groupRecognizer;
    this.newFields = newFields;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new ComposeRecord(recordTypeEvaluator, repeatingGroupName, groupRecognizer, newFields); 
    pipeline.addRecordFilter(recordFilter);
  }
}


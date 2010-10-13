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

package com.servingxml.components.parameter;

import junit.framework.TestCase;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.expr.substitution.ParameterSubstitutor;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.io.streamsink.OutputStreamSinkAdaptor;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.CommandLine;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.ValueType;
import com.servingxml.util.record.ValueTypeFactory;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;

public class ParameterTest extends TestCase {
  
  private MutableNameTable nameTable = new NameTableImpl();
  private AppContext appContext;
  private CommandLine commandLine = new CommandLine();
  private StreamSource defaultStreamSource = new InputStreamSourceAdaptor(System.in);
  private StreamSink defaultStreamSink = new OutputStreamSinkAdaptor(System.out);
  private Logger logger = SystemConfiguration.getLogger();
  
  public ParameterTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",resources,logger);
  }
  
  public void testParameter() throws Exception {
    QnameContext nameContext = new SimpleQnameContext(nameTable);
    
    Name myParamName = new QualifiedName("MyParam");
    SubstitutionExpr eval = SubstitutionExpr.parseString(nameContext,"Hello");
    ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(eval);
    ParameterDescriptor param = new ParameterDescriptorImpl(myParamName,valueEvaluator,ValueTypeFactory.STRING_TYPE);
    
    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    Record parameters = Record.EMPTY;
    
  }
  
  public void testRuntimeParameter() throws Exception {
    QnameContext nameContext = new SimpleQnameContext(nameTable);
    
    Name myParamName = new QualifiedName("MyParam");
    SubstitutionExpr eval = SubstitutionExpr.parseString(nameContext,"Hello");
    ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(eval);

    ParameterDescriptor param = new ParameterDescriptorImpl(myParamName,valueEvaluator,ValueTypeFactory.STRING_TYPE);
    
    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME);
    recordBuilder.setString(myParamName,"Hi");
    Record parameters = recordBuilder.toRecord();
    
  }
  
  public void testParameter2() throws Exception {
    
    Name myParamName = new QualifiedName("MyParam");
    Name yourParamName = new QualifiedName("YourParam");
    SubstitutionExpr eval = new ParameterSubstitutor(yourParamName);
    ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(eval);

    ParameterDescriptor param = new ParameterDescriptorImpl(myParamName,valueEvaluator,ValueTypeFactory.STRING_TYPE);
    
    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME);
    recordBuilder.setString(yourParamName,"Hello");
    Record parameters = recordBuilder.toRecord();
    
  }
  
  public void testRuntimeParameter2() throws Exception {
    Name myParamName = new QualifiedName("MyParam");
    Name yourParamName = new QualifiedName("YourParam");
    SubstitutionExpr eval = new ParameterSubstitutor(yourParamName);
    ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(eval);

    ParameterDescriptor param = new ParameterDescriptorImpl(myParamName,valueEvaluator,ValueTypeFactory.STRING_TYPE);
    
    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME);
    recordBuilder.setString(yourParamName,"Hello");
    recordBuilder.setString(myParamName,"Hi");
    Record parameters = recordBuilder.toRecord();
    
  }
}

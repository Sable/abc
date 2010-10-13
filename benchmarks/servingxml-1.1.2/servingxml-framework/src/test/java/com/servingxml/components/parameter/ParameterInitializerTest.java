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
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
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
import com.servingxml.util.record.Record;
import com.servingxml.util.record.ValueType;
import com.servingxml.util.record.ValueTypeFactory;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.ValueType;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.app.Environment;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.app.ParameterDescriptor;

public class ParameterInitializerTest extends TestCase {
  private static final Name COUNTRY_NAME = new QualifiedName("Country");
  private static final String CANADA = "Canada";
  private static final String USD = "United States";
  
  private MutableNameTable nameTable = new NameTableImpl();
  private AppContext appContext;
  private ServiceContext serviceContext;
  private CommandLine commandLine = new CommandLine();

  private StreamSource defaultStreamSource = new InputStreamSourceAdaptor(System.in);
  private StreamSink defaultStreamSink = new OutputStreamSinkAdaptor(System.out);
  private Logger logger = SystemConfiguration.getLogger();
  
  public ParameterInitializerTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",resources,logger);
    serviceContext = new DefaultServiceContext(appContext,"",logger);
  }
  public void testNoDefaults() throws Exception {
    ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
    RecordBuilder recordBuilder = new RecordBuilder(Name.EMPTY);
    recordBuilder.setString(COUNTRY_NAME,CANADA);
    Record parameters = recordBuilder.toRecord();
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, serviceContext, parameters, Record.EMPTY);
    //Flow flow = new FlowImpl(serviceContext, parameters, Record.EMPTY);
    Flow newFlow = flow.augmentParameters(serviceContext, parameterDescriptors);
    Record newParameters = newFlow.getParameters();
    String value = newParameters.getString(COUNTRY_NAME);
    assertTrue("value not null",value != null);
    assertTrue(value + "=" + CANADA,value.equals(CANADA));
    assertTrue("records are the same",newParameters == parameters);
  }

  public void testDefaults() throws Exception {
    QnameContext nameContext = new SimpleQnameContext(nameTable);
    SubstitutionExpr defaultEval = SubstitutionExpr.parseString(nameContext,CANADA);
    
    ValueEvaluator defaultValueEvaluator = new SubstitutionExprValueEvaluator(defaultEval);

    ValueEvaluator valueEvaluator = new DefaultParameterValueEvaluator(COUNTRY_NAME,defaultValueEvaluator);
    ParameterDescriptor parameterDescriptor = new ParameterDescriptorImpl(COUNTRY_NAME,valueEvaluator,
      ValueTypeFactory.STRING_TYPE);
    
    ParameterDescriptor[] parameterDescriptors = new ParameterDescriptor[]{parameterDescriptor};
    Record parameters = Record.EMPTY;
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, serviceContext, parameters, Record.EMPTY);

    Flow newFlow = flow.augmentParameters(serviceContext, parameterDescriptors);
    Record newParameters = newFlow.getParameters();
    String newValue = newParameters.getString(COUNTRY_NAME);
    String oldValue = parameters.getString(COUNTRY_NAME);
    assertTrue("newParameters newValue not null",newValue != null);
    assertTrue("old parameters value still null",oldValue == null);
    assertTrue(newValue + "=" + CANADA,newValue.equals(CANADA));
    assertTrue("records not the same",newParameters != parameters);
  }
  
  public void testOverrideDefaults() throws Exception {
    QnameContext nameContext = new SimpleQnameContext(nameTable);
    SubstitutionExpr defaultEval = SubstitutionExpr.parseString(nameContext,CANADA);

    ValueEvaluator defaultValueEvaluator = new SubstitutionExprValueEvaluator(defaultEval);

    ValueEvaluator valueEvaluator = new DefaultParameterValueEvaluator(COUNTRY_NAME, defaultValueEvaluator);
    ParameterDescriptor parameterDescriptor = new ParameterDescriptorImpl(COUNTRY_NAME,valueEvaluator,
      ValueTypeFactory.STRING_TYPE);
    
    ParameterDescriptor[] parameterDescriptors = new ParameterDescriptor[]{parameterDescriptor};
    
    RecordBuilder recordBuilder = new RecordBuilder(Name.EMPTY);
    recordBuilder.setString(COUNTRY_NAME,USD);
    Record parameters = recordBuilder.toRecord();
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, serviceContext, parameters, Record.EMPTY);
    Flow newFlow = flow.augmentParameters(serviceContext, parameterDescriptors);
    Record newParameters = newFlow.getParameters();
    String newValue = newParameters.getString(COUNTRY_NAME);
    String oldValue = parameters.getString(COUNTRY_NAME);
    assertTrue("newParameters value not null",newValue != null);
    assertTrue("old parameters value not null",oldValue != null);
    assertTrue(newValue + "=" + USD,newValue.equals(USD));
    assertTrue(oldValue + "=" + USD,oldValue.equals(USD));
    //assertTrue("records are the same",newParameters == parameters);
    assertTrue("values are the same",newValue == oldValue);
  }
}

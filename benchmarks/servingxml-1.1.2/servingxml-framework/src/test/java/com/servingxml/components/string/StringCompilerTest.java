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

package com.servingxml.components.string;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ServiceContext;
import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.ioc.resources.IocContainer;
import com.servingxml.ioc.resources.IocContainerFactory;
import com.servingxml.ioc.resources.MutableResourceTable;
import com.servingxml.ioc.resources.ResourceTableImpl;
import com.servingxml.ioc.resources.RootConfigurationContext;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.app.Environment;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.app.ParameterDescriptor;

import junit.framework.TestCase;

public class StringCompilerTest extends TestCase {
  private DocumentBuilderFactory documentBuilderFactory;
  private MutableNameTable nameTable;
  private MutableResourceTable resourceTable;
  private SAXTransformerFactory transformerFactory;
  private ComponentDictionary componentDictionary;
  private Logger logger = SystemConfiguration.getLogger();
  private AppContext appContext;
  
  public StringCompilerTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setValidating(false);
    documentBuilderFactory.setNamespaceAware(true);
    resourceTable = new ResourceTableImpl();

    IocContainerFactory containerFactory = new IocContainerFactory();
    containerFactory.loadComponentDefinitions();
    componentDictionary = containerFactory.getComponentDictionary();
    nameTable = containerFactory.getNameTable();

    transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer iocContainer = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",iocContainer,logger);

  }

  public void testLeadingTrailingSpace() throws Exception {
    String xmlString = "<x xmlns:sx=\"http://www.servingxml.com/ServingXML\"> Hello World </x>";
    DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(xmlString));
    Document document = builder.parse(inputSource);

    Record parameters = Record.EMPTY;

    RootConfigurationContext configContext = new RootConfigurationContext(
      document, nameTable, componentDictionary, resourceTable, "base", transformerFactory, parameters);

    StringFactory sf = StringFactoryCompiler.fromStringables(configContext, document.getDocumentElement());

    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);

    String expected = " Hello World ";
    String s = sf.createString(context, flow);
    assertTrue(s+"="+expected, s.equals(expected));
  }   

  public void testAllWhitespace() throws Exception {
    String xmlString = "<x xmlns:sx=\"http://www.servingxml.com/ServingXML\">         </x>";
    DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(xmlString));
    Document document = builder.parse(inputSource);

    Record parameters = Record.EMPTY;

    RootConfigurationContext configContext = new RootConfigurationContext(
      document, nameTable, componentDictionary, resourceTable, "base", transformerFactory, parameters);

    StringFactory sf = StringFactoryCompiler.fromStringables(configContext, document.getDocumentElement());

    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);

    String expected = "";
    String s = sf.createString(context, flow);
    assertTrue(s+"="+expected, s.equals(expected));
  }   

  public void testInlinedString() throws Exception {
    String xmlString = "<x xmlns:sx=\"http://www.servingxml.com/core\"><sx:toString value=\" Hello World \"/></x>";
    DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(xmlString));
    Document document = builder.parse(inputSource);

    Record parameters = Record.EMPTY;

    RootConfigurationContext configContext = new RootConfigurationContext(
      document, nameTable, componentDictionary, resourceTable, "base", transformerFactory, parameters);

    StringFactory sf = StringFactoryCompiler.fromStringables(configContext, document.getDocumentElement());

    ServiceContext context = new DefaultServiceContext(appContext,"",logger);
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);

    String expected = " Hello World ";
    String s = sf.createString(context, flow);
    assertTrue(s+"="+expected, s.equals(expected));
  }   

}

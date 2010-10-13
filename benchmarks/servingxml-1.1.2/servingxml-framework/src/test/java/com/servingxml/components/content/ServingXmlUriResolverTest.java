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

package com.servingxml.components.content;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QnameContext;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlUriResolverTest extends TestCase {
  private ServiceContext serviceContext = null;
  private QnameContext qnameContext = new SimpleQnameContext();

  public ServingXmlUriResolverTest(String name) {
    super(name);
  }
  protected void setUp() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    Name contentName = new QualifiedName("base/","default");

    OutputPropertyFactory[] defaultOutputProperties = new OutputPropertyFactory[0];
    resources.registerServiceComponent(Content.class,contentName.toUri(),new Document(defaultOutputProperties));

    AppContext appContext = new DefaultAppContext("test",resources);
    serviceContext = new DefaultServiceContext(appContext,"test");
  }

  public void testNilResolver() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME);
    Record parameters = recordBuilder.toRecord();

    ServingXmlUriResolver resolver = new ServingXmlUriResolver(qnameContext.getPrefixMap(), serviceContext, parameters, null, null);
    Source source = resolver.resolve("xxx","yyy");
    assertTrue("",source == null);
  }

  public void testResolver() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME);
    Record parameters = recordBuilder.toRecord();

    ServingXmlUriResolver resolver = new ServingXmlUriResolver(qnameContext.getPrefixMap(), serviceContext, parameters, null, null);
    Source source = resolver.resolve("default","base/");
    assertTrue("default",source != null);
    assertTrue("default",source instanceof SAXSource);
  }

  public void testDocumentBase() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME);
    Record parameters = recordBuilder.toRecord();

    ServingXmlUriResolver resolver = new ServingXmlUriResolver(qnameContext.getPrefixMap(), serviceContext, parameters, "base/", null);
    Source source = resolver.resolve("default","b");
    assertTrue("default",source != null);
    assertTrue("default",source instanceof SAXSource);
  }
}                    


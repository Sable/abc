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

package com.servingxml.components.recordmapping;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.helpers.AttributesImpl;

import junit.framework.TestCase;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ServiceContext;
import com.servingxml.ioc.resources.ResourceTableImpl;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.app.Environment;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.app.ParameterDescriptor;

public class MultipleGroupingRecordMapContainerTest extends TestCase {
  Name fieldName = new QualifiedName("field");

  private Logger logger = SystemConfiguration.getLogger();
  private AppContext appContext;
  private MutableNameTable nameTable = new NameTableImpl();
  private ServiceContext context;

  public MultipleGroupingRecordMapContainerTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",resources,logger);
    context = new DefaultServiceContext(appContext,"",logger);
  }
  
  public void testRecordMapping() 
  throws Exception {
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    Flow flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);

    MapXml[] siblings = new MapXml[]{};

  }

  class ARecordMap implements MapXml {
    boolean started = false;

    public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
    }
    public boolean isGrouping() {
      return started;
    }
    public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, ExtendedContentHandler handler, GroupState groupListener) {
    }
    public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, ExtendedContentHandler handler, Record variables) {
    }
    public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    }
  }
}

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
 
package com.servingxml.components.flatfile.options;

import java.nio.charset.Charset;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Environment;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.app.Environment;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.app.ParameterDescriptor;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileOptionsTest extends TestCase {

  private Logger logger = SystemConfiguration.getLogger();
  private AppContext appContext;
  private MutableNameTable nameTable = new NameTableImpl();
  private ServiceContext context;
  private Flow flow;

  public FlatFileOptionsTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",resources,logger);
    context = new DefaultServiceContext(appContext,"",logger);
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);
  }

  public void testEmptyFieldDefaults() throws Exception {
    FlatFileOptionsFactory flatFileOptionsFactory = new FlatFileOptionsFactory();
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, true, false, Charset.defaultCharset());
    assertTrue(flatFileOptions.getSegmentDelimiters().length == 0);
    assertTrue(flatFileOptions.getRepeatDelimiters().length == 0);
    assertTrue(flatFileOptions.getFieldDelimiters().length == 0);
    assertTrue(flatFileOptions.getSubfieldDelimiters().length == 0);
  }

  public void testFieldDefaults() throws Exception {
    FlatFileOptionsFactory fieldDefaults1 = new FlatFileOptionsFactory();

    SegmentDelimiterFactory[] segmentDelimiterFactories1 = new SegmentDelimiterFactory[]{new SegmentDelimiterFactory("|")};
    RepeatDelimiterFactory[] repeatDelimiterFactories1 = new RepeatDelimiterFactory[]{new RepeatDelimiterFactory("~")};
    FieldDelimiterFactory[] fieldDelimiterFactories1 = new FieldDelimiterFactory[]{new FieldDelimiterFactory("^")};
    SubfieldDelimiterFactory[] subfieldDelimiterFactories1 = new SubfieldDelimiterFactory[]{new SubfieldDelimiterFactory(";")};

    fieldDefaults1.setSegmentDelimiterFactories(segmentDelimiterFactories1);
    fieldDefaults1.setRepeatDelimiterFactories(repeatDelimiterFactories1);
    fieldDefaults1.setFieldDelimiterFactories(fieldDelimiterFactories1);
    fieldDefaults1.setSubfieldDelimiterFactories(subfieldDelimiterFactories1);

    FlatFileOptions fieldSettings1 = fieldDefaults1.createFlatFileOptions(context, flow, true, false, Charset.defaultCharset());
    assertTrue("is segment1", fieldSettings1.getSegmentDelimiters().length == 1);
    assertTrue("is repeat1", fieldSettings1.getRepeatDelimiters().length == 1);
    assertTrue("is field1", fieldSettings1.getFieldDelimiters().length == 1);
    assertTrue("is subfield1", fieldSettings1.getSubfieldDelimiters().length == 1);

    //assertTrue("~", fieldSettings1.getSegmentDelimiters()[0].charAt(0) == '|');
    //assertTrue("|", fieldSettings1.getRepeatDelimiters()[0].charAt(0) == '~');
    //assertTrue("^", fieldSettings1.getFieldDelimiters()[0].charAt(0) == '^');
    //assertTrue(";", fieldSettings1.getSubfieldDelimiters()[0].charAt(0) == ';');

    FlatFileOptionsFactory fieldDefaults2 = new FlatFileOptionsFactory();
    FieldDelimiterFactory[] fieldDelimiterFactories2 = new FieldDelimiterFactory[]{new FieldDelimiterFactory("")};
    SubfieldDelimiterFactory[] subfieldDelimiterFactories2 = new SubfieldDelimiterFactory[]{new SubfieldDelimiterFactory("")};
    RepeatDelimiterFactory[] repeatDelimiterFactories2 = new RepeatDelimiterFactory[]{new RepeatDelimiterFactory("")};
    SegmentDelimiterFactory[] segmentDelimiterFactories2 = new SegmentDelimiterFactory[]{new SegmentDelimiterFactory("")};

    fieldDefaults2.setSegmentDelimiterFactories(segmentDelimiterFactories2);
    fieldDefaults2.setRepeatDelimiterFactories(repeatDelimiterFactories2);
    fieldDefaults2.setFieldDelimiterFactories(fieldDelimiterFactories2);
    fieldDefaults2.setSubfieldDelimiterFactories(subfieldDelimiterFactories2);

    FlatFileOptions fieldSettings2 = fieldDefaults2.createFlatFileOptions(context, flow, fieldSettings1);
    assertTrue("is segment2",fieldSettings2.getSegmentDelimiters().length == 0);
    assertTrue("is repeat2",fieldSettings2.getRepeatDelimiters().length == 0);
    assertTrue("is field2",fieldSettings2.getFieldDelimiters().length == 0);
    assertTrue("is subfield2",fieldSettings2.getSubfieldDelimiters().length == 0);
  }
}                    


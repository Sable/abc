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

package com.servingxml.components;

import java.io.StringWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Content;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.OutputStreamSinkAdaptor;

/**
 *
 * 
 * @author Daniel A. Pardaniel.parker@servingxml.com)
 */

public class DocumentFilterTest extends TestCase {

  private static final String myNamespaceUri = "http://mycompany.com/mynames/";
  private StreamSource defaultStreamSource = new InputStreamSourceAdaptor(System.in);
  private StreamSink defaultStreamSink = new OutputStreamSinkAdaptor(System.out);

  public DocumentFilterTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testDocumentFilter() throws Exception {
/*
    SimpleIocContainer resources = new SimpleIocContainer();
    Content content = resources.lookupServiceComponent(CONTENT_ID,myNamespaceUri,"books");
    RecordBuilder parameters = new RecordBuilder();
    parameters.setString("category","F");
    ServiceContext context = new DefaultServiceContext("books",null,"",defaultStreamSource,defaultStreamSink);
    SAXSource source = content.newSAXSource(context,parameters);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    //System.out.println (writer.toString());
*/    
  }

  public void testDocumentFilter2() throws Exception {
/*
    SimpleIocContainer resources = new SimpleIocContainer();
    Content content = resources.lookupServiceComponent(CONTENT_ID,myNamespaceUri,"categories");
    Record parameters = new RecordBuilder();
    ServiceContext context = new DefaultServiceContext("books",null,"",defaultStreamSource,defaultStreamSink);
    SAXSource source = content.newSAXSource(context,parameters);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    //System.out.println (writer.toString());
*/    
  }
}                    


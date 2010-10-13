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

package com.servingxml.components.content.dynamic;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.servingxml.app.DefaultServiceContext;
import com.servingxml.components.content.dynamic.KeyIdentifier;
import com.servingxml.components.content.dynamic.KeyIdentifierImpl;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.CachedContent;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.record.RecordBuilder;
import samples.books.BookCatalog;
import com.servingxml.io.cache.RevalidationType;
import com.servingxml.util.CommandLine;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.OutputStreamSinkAdaptor;
import com.servingxml.util.QualifiedName;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DynamicContentTest extends TestCase {

  private static final String myNamespaceUri = "http://mycompany.com/mynames/";

  private CommandLine commandLine = new CommandLine();
  private StreamSource defaultStreamSource = new InputStreamSourceAdaptor(System.in);
  private StreamSink defaultStreamSink = new OutputStreamSinkAdaptor(System.out);

  public DynamicContentTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testUncachedDynamicContent() throws Exception {
/*
    Content booksAccessor = makeBooksContentAccessor();

    RecordBuilder parameters = new RecordBuilder();
    parameters.setString("category","F");
    SimpleIocContainer resources = new SimpleIocContainer();
    OutputStream outputStream = new ByteArrayOutputStream();
    ServiceContext context = new DefaultServiceContext("books",resources,"text/html",
      defaultStreamSource,defaultStreamSink);

    SAXSource source = booksAccessor.newSAXSource(context,parameters);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    //System.out.println (writer.toString());
*/    
  }
/*
  public void testCachedDynamicContent() throws Exception {
    Name documentName = new QualifiedName("books");
    DynamicContentHandler booksHandler = new BookCatalog();
    DynamicContent booksHandlerProxy = new DynamicContent(documentName,booksHandler); 

    DynamicChangeable expirable = new DynamicChangeableProxy(booksHandler);
    RecordBuilder parameters = new RecordBuilder();
    parameters.setString("category","F");

    KeyIdentifier identifier = new KeyIdentifierImpl(parameters);

    RevalidationType revalidationType = new RevalidationType(true,true);
    Content booksAccessor = new CachedDynamicContent(documentName,
      booksHandlerProxy,expirable,identifier,revalidationType);

    SimpleIocContainer resources = new SimpleIocContainer();
    OutputStream outputStream = new ByteArrayOutputStream();
    ServiceContext context = new DefaultServiceContext("books",resources,"text/html",
      defaultStreamSource,defaultStreamSink);

    SAXSource source = booksAccessor.newSAXSource(context,parameters);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    //System.out.println (writer.toString());
  }
*/
  public void testCachedDynamicContent2() throws Exception {
/*
    Content accessor = makeBooksContentAccessor(); 
    RevalidationType revalidationType = new RevalidationType(true,true);
    Content booksAccessor = new CachedContent(accessor,revalidationType);

    RecordBuilder parameters = new RecordBuilder();
    parameters.setString("category","F");
    SimpleIocContainer resources = new SimpleIocContainer();
    OutputStream outputStream = new ByteArrayOutputStream();
    ServiceContext context = new DefaultServiceContext("books",resources,"text/html",
      defaultStreamSource,defaultStreamSink);

    SAXSource source = booksAccessor.newSAXSource(context,parameters);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);
    transformer.transform(source,result);
    //System.out.println (writer.toString());
*/    
  }

/*
  private Content makeBooksContentAccessor() throws Exception {
    Name documentName = new QualifiedName("books");
    DynamicContentHandler booksHandler = new BookCatalog();

    RecordBuilder parameters = new RecordBuilder();
    parameters.setString("category","F");
    KeyIdentifier identifier = new KeyIdentifierImpl(parameters);
    Content booksAccessor = new DynamicContent(documentName,booksHandler,identifier); 

    return booksAccessor;
  }
*/  
}                    


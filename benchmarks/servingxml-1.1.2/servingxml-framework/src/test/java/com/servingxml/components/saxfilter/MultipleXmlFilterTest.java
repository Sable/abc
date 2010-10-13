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

package com.servingxml.components.saxfilter;

import java.io.IOException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.AbstractXmlReader;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultipleXmlFilterTest extends TestCase {

  public MultipleXmlFilterTest(String name) {
    super(name);
  }

  public void testMultipleXmlFilter() throws Exception {
    MultipleXmlFilter multipleFilter = new MultipleXmlFilter();
    XMLFilter filter1 = new MyFilter("filter1");
    XMLFilter filter2 = new MyFilter("filter2");
    multipleFilter.addXmlFilter(filter1);
    // parent of filter1 is null
    // head = tail = filter1

    multipleFilter.addXmlFilter(filter2);
    // parent of filter1 is null
    // parent of filter2 is filter1
    // head = filter1
    // tail = filter2

    XMLReader reader = new MyXmlReader();

    ContentHandler handler = new MyContentHandler();
    multipleFilter.setContentHandler(handler);
    multipleFilter.setParent(reader);
    //  parent of filter1 is reader
    // parent of filter2 is filter1
    
    multipleFilter.parse("");
  }

  static class MyXmlReader extends AbstractXmlReader {
    public void parse(String systemId) throws IOException,SAXException {
      getContentHandler().startDocument();
      getContentHandler().startElement("","foo","foo",SystemConstants.EMPTY_ATTRIBUTES);
      getContentHandler().endElement("","foo","foo");
      getContentHandler().endDocument();
    }
  }

  static class MyContentHandler extends XMLFilterImpl {
    public void startElement(String uri, String localName, String qname, Attributes atts) 
    throws SAXException {
      System.out.println("ContentHandler startElement: name="+localName);
    }
    public void endElement(String uri, String localName, String qname) 
    throws SAXException {
      System.out.println("ContentHandler endElement: name="+localName);
    }
  }

  static class MyFilter extends XMLFilterImpl {
    private String name;

    MyFilter(String name) {
      this.name = name;
    }

    public void startElement(String uri, String localName, String qname, Attributes atts) 
    throws SAXException {
      System.out.println("Filter " + name + ": startElement: name="+localName);
      super.startElement(uri,localName,qname,atts);
    }
    public void endElement(String uri, String localName, String qname) 
    throws SAXException {
      System.out.println("Filter " + name + ": endElement: name="+localName);
      super.endElement(uri,localName,qname);
    }
  }
}                    


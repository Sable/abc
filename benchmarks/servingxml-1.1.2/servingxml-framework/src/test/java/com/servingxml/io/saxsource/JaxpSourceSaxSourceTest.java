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

package com.servingxml.io.saxsource;

import java.io.StringReader;
import java.io.Reader;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.StringStreamSink;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class JaxpSourceSaxSourceTest extends TestCase {

  public JaxpSourceSaxSourceTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testFromSource() throws Exception {

    String input = "<document><customer>Amanda</customer></document>";
    Reader reader = new StringReader(input);

    Source source = new StreamSource(reader,"");

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    SaxSource saxSource = new JaxpSourceSaxSource(source, transformerFactory);

    XMLReader xmlReader = saxSource.createXmlReader();

    StringStreamSink streamSink = new StringStreamSink();
    OutputStream os = streamSink.getOutputStream();

    Transformer transformer = transformerFactory.newTransformer();
    Result result = new StreamResult(os);

    transformer.transform(source,result);

    String output = streamSink.toString();

    assertTrue(input + "=" + output, output.endsWith(input));

    streamSink.close();
  }
}                    


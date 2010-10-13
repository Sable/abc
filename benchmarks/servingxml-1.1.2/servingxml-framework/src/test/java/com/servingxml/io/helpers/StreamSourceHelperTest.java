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

package com.servingxml.io.helpers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.ByteArrayInputStream;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.InputSource;

import org.w3c.dom.Document;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsink.StringStreamSink;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.DefaultJaxpStreamSource;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.io.streamsource.url.UrlSource;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class StreamSourceHelperTest extends TestCase {

  public StreamSourceHelperTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testDomSource() throws Exception {
    String input = "<Swath><Shape>Polygon</Shape><GeoPoint><lat>1</lat><lon>2</lon></GeoPoint><GeoPoint><lat>3</lat><lon>4</lon></GeoPoint><GeoPoint><lat>5</lat><lon>6</lon></GeoPoint></Swath>";
    InputSource inputSource = new InputSource(new StringReader(input));
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document doc = builder.parse(inputSource);
    Source source = new DOMSource(doc);
    StreamSource streamSource = StreamSourceHelper.fromJaxpSource(source);
    assertTrue("dom",streamSource instanceof DefaultJaxpStreamSource);
    String output = streamSourceToString(streamSource);

    assertTrue(output,output.endsWith(input));
  }

  public void testReaderSource() throws Exception {
    String input = "H002FHaruki Murakami               Kafka on the Shore                 25.17";
    Reader reader = new StringReader(input);

    String systemId = "http://www.servingxml.com";
    Source source = new javax.xml.transform.stream.StreamSource(reader,systemId);

    StreamSource streamSource = StreamSourceHelper.fromJaxpSource(source);
    assertTrue(streamSource.getClass().getName(),streamSource instanceof InputStreamSourceAdaptor);
    assertTrue(streamSource.getSystemId().equals(systemId));

    String output = streamSourceToString(streamSource);

    assertTrue(output,output.equals(input));
  }

  public void testInputStreamSource() throws Exception {
    String input = "H002FHaruki Murakami               Kafka on the Shore                 25.17";
    byte[] bytes = input.getBytes();
    InputStream is = new ByteArrayInputStream(bytes);

    String systemId = "http://www.servingxml.com";
    Source source = new javax.xml.transform.stream.StreamSource(is,systemId);

    StreamSource streamSource = StreamSourceHelper.fromJaxpSource(source);
    assertTrue(streamSource.getClass().getName(),streamSource instanceof InputStreamSourceAdaptor);
    assertTrue(streamSource.getSystemId().equals(systemId));

    String output = streamSourceToString(streamSource);
    assertTrue(output,output.equals(input));
  }

  private String streamSourceToString(StreamSource streamSource) throws Exception {
    InputStream is = streamSource.openStream();
    Reader reader = null;
    if (streamSource.getCharset() != null) {
      reader = new InputStreamReader(is,streamSource.getCharset());
    } else {
      reader = new InputStreamReader(is);
    }
    StringBuilder buf = new StringBuilder();
    boolean done = false;
    while (!done) {
      int c = reader.read();
      if (c == -1) {
        done = true;
      } else {
        buf.append((char)c);
      }
    }
    streamSource.closeStream(is);

    return buf.toString();
  }
}                    


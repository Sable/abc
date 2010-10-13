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

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.util.ServingXmlException;        

public class StreamSourceSaxSource implements SaxSource {
  private final StreamSource streamSource;
  private Properties outputProperties = new Properties();

  public StreamSourceSaxSource(StreamSource streamSource) {
    this.streamSource = streamSource;
  }

  public XMLReader createXmlReader() {

    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setNamespaceAware(true);
      parserFactory.setValidating(false);
      SAXParser parser = parserFactory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      XMLReader readerAdaptor = new StreamXmlReaderAdaptor(reader,streamSource);
      return readerAdaptor;
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (org.xml.sax.SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public String getSystemId() {
    return streamSource.getSystemId();
  }

  public Key getKey() {
    return streamSource.getKey();
  }

  public Expirable getExpirable() {
    return streamSource.getExpirable();
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    InputStream is = null;
    try {
      is = streamSource.openStream();
      javax.xml.transform.stream.StreamSource source;
      if (streamSource.getCharset() != null ) {
        Reader reader = new BufferedReader(new InputStreamReader(is, streamSource.getCharset()));
        source = new javax.xml.transform.stream.StreamSource(reader,streamSource.getSystemId());
      }
      else{
        //  This should be an input stream, not a reader, to use XML encoding
        source = new javax.xml.transform.stream.StreamSource(new BufferedInputStream(is),streamSource.getSystemId());
      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      Result result = new StreamResult(writer);
      transformer.transform(source,result);
    } catch (Exception e) {
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (Exception e) {
      }
    }
    return writer.toString();
  }

  public Properties getDefaultOutputProperties() {
    return outputProperties;
  }
}


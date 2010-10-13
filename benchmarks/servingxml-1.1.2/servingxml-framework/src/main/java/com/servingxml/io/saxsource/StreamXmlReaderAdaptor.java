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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsource.StreamSource;

public class StreamXmlReaderAdaptor implements XMLReader {
  private final StreamSource streamSource;
  private final XMLReader reader;
  
  public StreamXmlReaderAdaptor(XMLReader reader, StreamSource streamSource) {
    this.reader = reader;
    this.streamSource = streamSource;
  }

  public boolean getFeature(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    return reader.getFeature(name);
  }

  public void setFeature(String name, boolean value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    reader.setFeature(name,value);
  }
  
  public Object getProperty(String name)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    return reader.getProperty(name);
  }

  public void setProperty(String name, Object value)
  throws SAXNotRecognizedException, SAXNotSupportedException {
    reader.setProperty(name,value);
  }

  public void setEntityResolver(EntityResolver resolver) {
    reader.setEntityResolver(resolver);
  }

  public EntityResolver getEntityResolver() {
    return reader.getEntityResolver();
  }

  public void setDTDHandler(DTDHandler handler) {
    reader.setDTDHandler(handler);
  }
  public DTDHandler getDTDHandler() {
    return reader.getDTDHandler();
  }
  public ContentHandler getContentHandler() {
    return reader.getContentHandler();
  }

  public void setContentHandler(ContentHandler handler) {
    reader.setContentHandler(handler);
  }
  public void setErrorHandler(ErrorHandler handler) {
    reader.setErrorHandler(handler);
  }
  
  public ErrorHandler getErrorHandler() {
    return reader.getErrorHandler();
  }

  public void parse(InputSource input) throws IOException, SAXException {
    parse("");
  }

  public void parse(String href) throws IOException, SAXException {
    InputStream is = null;
    try {
      is = streamSource.openStream();
      String systemId = streamSource.getSystemId();
      InputSource inputSource = null;
      if(streamSource.getCharset() != null ){
        inputSource = new InputSource(new BufferedReader(new InputStreamReader(is, streamSource.getCharset())));
      }
      else{
        //  This should be an input stream, not a reader, to use XML encoding
        inputSource = new InputSource(new BufferedInputStream(is));
      }

      inputSource.setSystemId(systemId);
      reader.parse(inputSource);
    } catch (ServingXmlException e) {
      throw new IOException(e.getMessage());
    } finally {
      streamSource.closeStream(is);
    }
  }
}


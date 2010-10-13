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

package com.servingxml.io.streamsink;

import java.io.OutputStream;
import java.util.Properties;
import java.nio.charset.Charset;

import com.servingxml.util.ServingXmlException;

public class OutputStreamSinkAdaptor extends AbstractStreamSink implements StreamSink {

  private Properties outputProperties = new Properties();
  private final OutputStream os;
  private final Charset charset;

  public OutputStreamSinkAdaptor(OutputStream os) {
    this(os, null);
  }

  public OutputStreamSinkAdaptor(OutputStream outputStream, Charset charset) {
    this.os = outputStream;
    this.charset = charset;
  }

  public OutputStream getOutputStream() {
    return os;
  }

  public void setOutputProperties(Properties outputProperties) {
    this.outputProperties = outputProperties;
  }

  public void setOutputProperty(String key, String value) {
    this.outputProperties.setProperty(key,value);
  }

  public Properties getOutputProperties() {
    return outputProperties;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public void close() {
  }
}


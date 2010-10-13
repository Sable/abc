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

package com.servingxml.components.command; 

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsink.AbstractStreamSink;

public class CommandSink extends AbstractStreamSink implements StreamSink {
  private final ProcessBuilder processBuilder;
  private final OutputStream os;
  private final Charset charset;
  private Properties outputProperties = new Properties();

  public CommandSink(ProcessBuilder processBuilder) {
    this(processBuilder, Charset.defaultCharset());
  }

  public CommandSink(ProcessBuilder processBuilder, Charset charset) {
    this.processBuilder = processBuilder;
    this.charset = charset;
    try {
      Process process = processBuilder.start();
      os = process.getOutputStream();
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public OutputStream getOutputStream() {
    return os;
  }

  public void close() {
    try {
      if (os != null) {
        os.close();
      }
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
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
}

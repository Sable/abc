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

package com.servingxml.components.streamsink; 

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import com.servingxml.io.streamsink.AbstractStreamSink;

import com.servingxml.io.streamsink.StreamSink;

class DefaultStreamSink extends AbstractStreamSink implements StreamSink {

  private final StreamSink sink;
  private final Charset charset;

  DefaultStreamSink(StreamSink sink, Charset charset) {
    this.sink = sink;
    this.charset = charset == null ? sink.getCharset() : charset;
  }

  public OutputStream getOutputStream() {
    return sink.getOutputStream();
  }

  public void close() {
    sink.close();
  }

  public void setOutputProperties(Properties outputProperties) {
    sink.setOutputProperties(outputProperties);
  }

  public void setOutputProperty(String key, String value) {
    sink.setOutputProperty(key, value);
  }

  public Properties getOutputProperties() {
    return sink.getOutputProperties();
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}

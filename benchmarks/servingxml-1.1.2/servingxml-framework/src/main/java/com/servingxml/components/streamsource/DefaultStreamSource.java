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

package com.servingxml.components.streamsource; 

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;

class DefaultStreamSource implements StreamSource {

  private final StreamSource source;
  private final Charset charset;

  DefaultStreamSource(StreamSource source, Charset charset) {
    this.source = source;
    this.charset = charset == null ? source.getCharset() : charset;
  }

  public InputStream openStream() {
    return source.openStream();
  }

  public void closeStream(InputStream os) throws IOException {
    source.closeStream(os);
  }

  public Key getKey() {
    return source.getKey();
  }

  public Expirable getExpirable() {
    return source.getExpirable();
  }

  public String getSystemId() {
    return source.getSystemId();
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}

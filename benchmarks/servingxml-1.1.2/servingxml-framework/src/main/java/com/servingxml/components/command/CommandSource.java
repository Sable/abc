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
import java.io.InputStream;
import java.nio.charset.Charset;

import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.util.ServingXmlException;

public class CommandSource implements StreamSource {
  private final ProcessBuilder processBuilder;
  private final String systemId;
  private final Key key;
  private final Charset charset;

  public CommandSource(ProcessBuilder processBuilder) {
    this(processBuilder, null);
  }

  public CommandSource(ProcessBuilder processBuilder, Charset charset) {
    this.processBuilder = processBuilder;
    this.key = DefaultKey.newInstance();
    this.systemId = key.toString();
    this.charset = charset;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public InputStream openStream() {
    try {
      Process process = processBuilder.start();
      InputStream is = process.getInputStream();
      return is;
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
  
  public Key getKey() {
    return key;
  }
  
  public Expirable getExpirable() {
    return Expirable.NEVER_EXPIRES;
  }
  
  public String getSystemId() {
    return systemId;
  }
  
  public void closeStream(InputStream is) throws IOException {
    if (is != null) {
      is.close();
    }
  }
}

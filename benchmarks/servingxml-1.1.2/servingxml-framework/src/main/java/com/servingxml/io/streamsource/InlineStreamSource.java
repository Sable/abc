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

package com.servingxml.io.streamsource; 

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.util.CharsetHelper;

public class InlineStreamSource implements StreamSource {
  private final byte[] bytes;
  private final String systemId;
  private final Key key;
  private final Charset charset;
  
  public InlineStreamSource(String str) {
    this(str, (Charset)null);
  }

  public InlineStreamSource(String str, Charset charset) {
    //System.out.println(str);
    this.bytes = CharsetHelper.stringToBytes(str,charset);
    this.key = DefaultKey.newInstance();
    this.systemId = key.toString();
    this.charset = charset;
  }

  public InlineStreamSource(String str, String encoding) {
    this.bytes = CharsetHelper.stringToBytes(str,encoding);
    this.key = DefaultKey.newInstance();
    this.systemId = key.toString();
    this.charset = Charset.forName(encoding);
  }
  
  public InputStream openStream() {
    InputStream is = new ByteArrayInputStream(bytes);
    return is;
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

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}

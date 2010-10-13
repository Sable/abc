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

import java.io.InputStream;
import java.nio.charset.Charset;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;

public class InputStreamSourceAdaptor implements StreamSource {
  private final InputStream is;
  private final String systemId;
  private final Key key;
  private final Charset charset;
  
  public InputStreamSourceAdaptor(InputStream is) {
    this(is, (Charset)null);
  }

  public InputStreamSourceAdaptor(InputStream is, Charset charset) {
    this.charset = charset;
    this.is = is;
    this.key = DefaultKey.newInstance();
    this.systemId = key.toString();
  }

  //  revisit
  public InputStreamSourceAdaptor(InputStream is, String systemId) {
    this(is,systemId,null);
  }

  //  revisit
  public InputStreamSourceAdaptor(InputStream is, String systemId, Charset charset) {
    this.charset = charset;
    this.is = is;
    this.systemId = systemId;
    this.key = DefaultKey.newInstance();
  }
  
  public InputStream openStream() {
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
  
  public void closeStream(InputStream is) {
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}                                          


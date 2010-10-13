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

package com.servingxml.io.streamsource.url; 

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.streamsource.StreamExpirable;
import com.servingxml.io.streamsource.SystemIdKey;
import com.servingxml.io.streamsource.file.FileSource;
import com.servingxml.util.ServingXmlException;

public class UrlSource implements StreamSource {
  private final URL url;
  private final Charset charset;
    
  public UrlSource(URL url) {
    this(url, null);
  }
  
  public UrlSource(URL url, Charset charset) {
    this.url = url;
    this.charset = charset;
  }
  
  public InputStream openStream() {
    try {
      InputStream is = url.openStream();
      return is;
    } catch (IOException e) {
      String message = "Failed attempting to open URL " + url.toString() + ".  " + e.getMessage();
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
  
  public Key getKey() {
    Key key = new SystemIdKey(url.toString());
    return key;
  }
  
  public Expirable getExpirable() {
    Expirable expirable = createStreamExpirable(url);
    return expirable;
  }
  
  public String getSystemId() {
    return url.toString();
  }
  
  public void closeStream(InputStream is) throws IOException {
    if (is != null) {
      is.close();
    }
  }

  public static final StreamExpirable createStreamExpirable(URL url) {

    String protocol = url.getProtocol();

    //  There is a bug in the java.net.URL "file:" protocol implementation of the
    // getLastModified() method; it always returns 0.  We therefore use the 
    // FileExpirable specialization for the "file:" protocol case.
    StreamExpirable changeable = null;
    if (protocol.equals("file")) {
      String fileName = url.getFile();
      File file = new File(fileName);
      changeable = FileSource.createStreamExpirable(file);
    } else {
      changeable = new UrlExpirable(url);
    }
    
    return changeable;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

}

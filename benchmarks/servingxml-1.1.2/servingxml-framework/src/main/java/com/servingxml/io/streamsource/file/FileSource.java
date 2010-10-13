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

package com.servingxml.io.streamsource.file; 

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.streamsource.StreamExpirable;
import com.servingxml.io.streamsource.SystemIdKey;
import com.servingxml.util.ServingXmlException;

public class FileSource implements StreamSource {
  private final File file;
  private final String systemId;
  private final Charset charset;
  
  public FileSource(File file, Charset charset) {
    this.charset = charset;
    this.file = file;
    try {
      URI uri = file.toURI();
      URL url = uri.toURL();
      systemId = url.toString();
    } catch (MalformedURLException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
  
  public FileSource(File file) {
    this(file,null);
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
                                                       
  public File getFile() {
    return file;
  }
  
  public InputStream openStream() {
    try {
      InputStream is = new FileInputStream(file);
      return is;
    } catch (IOException e) {
      String message = "Failed attempting to open file " + file.getPath() + ".  " + e.getMessage();
      throw new ServingXmlException(message,e);
    }
  }
  
  public void closeStream(InputStream is) throws IOException {
    if (is != null) {                         
      is.close();
    }
  }
  
  public Key getKey() {
    Key key = new SystemIdKey(systemId);
    return key;
  }
  
  public Expirable getExpirable() {
    Expirable expirable = new FileExpirable(file);
    return expirable;
  }
  
  public String getSystemId() {
    return systemId;
  }

  public static final StreamExpirable createStreamExpirable(File file) {
    
    return new FileExpirable(file);
  }
}

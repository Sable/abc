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

import java.nio.charset.Charset;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.util.ServingXmlException;

public class DefaultJaxpStreamSource implements StreamSource {
  private final Source source;
  private final String systemId;
  private final Key key;
  private final Charset charset;
  
  public DefaultJaxpStreamSource(Source source) {
    this.source = source;
    this.key = DefaultKey.newInstance();
    if (source instanceof javax.xml.transform.stream.StreamSource) {
      javax.xml.transform.stream.StreamSource stream = (javax.xml.transform.stream.StreamSource)source;
      this.systemId = stream.getSystemId() == null ? key.toString() : stream.getSystemId();
    } else {
      this.systemId = key.toString();
    }
    this.charset = null;
  }
  
  public InputStream openStream() {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      Result result = new StreamResult(os);
      transformer.transform(source,result);
      return new ByteArrayInputStream(os.toByteArray());
    } catch (TransformerException e) {
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

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}

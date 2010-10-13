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
                                                
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.nio.charset.Charset;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.CharsetHelper;

public class StringStreamSink extends AbstractStreamSink implements StreamSink {
  private final ByteArrayOutputStream os;
  private final Charset charset;

  public StringStreamSink() {
    this((Charset)null);
  }

  public StringStreamSink(String encoding) {
    this.os = new ByteArrayOutputStream();
    this.charset = Charset.forName(encoding);
  }

  public StringStreamSink(Charset charset) {
    this.os = new ByteArrayOutputStream();
    this.charset = charset;
  }

  public String toString() {
    try {
      byte[] bytes = os.toByteArray();
      String s = CharsetHelper.bytesToString(bytes,charset);
      return s;
    } catch(ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public OutputStream getOutputStream() {   
    return os;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
  
  public void close() { 
    try {
      os.close();
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void setOutputProperties(Properties outputProperties) {
  }

  public void setOutputProperty(String key, String value) {
  }

  public Properties getOutputProperties() {
    return new Properties();
  }
}                    


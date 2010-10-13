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

package com.servingxml.io.saxsink;

import java.util.Properties;

import org.xml.sax.ContentHandler;

import com.servingxml.util.xml.NullContentHandler;

public interface SaxSink {

  static final SaxSink NULL = new NullSaxSink();

  ContentHandler getContentHandler();

  void close();

  void setOutputProperties(Properties outputProperties);
}                    

final class NullSaxSink implements SaxSink {
  private static final ContentHandler NULL_CONTENT_HANDLER = new NullContentHandler();

  public final ContentHandler getContentHandler() {
    return NULL_CONTENT_HANDLER;
  }

  public final void close() {
  }

  public void setOutputProperties(Properties outputProperties) {
  }
}

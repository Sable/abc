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

package com.servingxml.components.saxsource;

import java.util.Properties;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import com.servingxml.io.saxsource.AbstractSaxSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;

/**
 * Factory for creating a XMLReader. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SaxEventBufferSource extends AbstractSaxSource {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};
  private final SaxEventBuffer buffer;
  private final String systemId;
  private final Key key;

  public SaxEventBufferSource(SaxEventBuffer buffer, String systemId, TransformerFactory transformerFactory) {
    super(transformerFactory);
    this.buffer = buffer;
    this.systemId = systemId;
    this.key = DefaultKey.newInstance();
  }

  public XMLReader createXmlReader() {
    return buffer.createXmlReader();
  }

  public Key getKey() {
    return key;
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public String getSystemId() {
    return systemId;
  }
}

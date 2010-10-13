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

package com.servingxml.io.saxsource;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.SourceXmlReader;

/**
 * Implements a simple SaxSource. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class JaxpSourceSaxSource extends AbstractSaxSource {
  private final Source source;
  private final String systemId;
  private final Key key;
  private final Expirable expirable;
  private final TransformerFactory transformerFactory;

  public JaxpSourceSaxSource(Source source, TransformerFactory transformerFactory) {
    super(transformerFactory);
    this.source = source;
    this.key = DefaultKey.newInstance();
    this.systemId = key.toString();
    this.expirable = Expirable.IMMEDIATE_EXPIRY;
    this.transformerFactory = transformerFactory;
  }

  public XMLReader createXmlReader() {
    return new SourceXmlReader(source);
  }

  public Key getKey() {
    return key;
  }

  public Expirable getExpirable() {
    return expirable;
  }

  public String getSystemId() {
    return systemId;
  }

  public InputStream openStream() {
    InputStream is;
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      Transformer transformer = transformerFactory.newTransformer();
      Result result = new StreamResult(os);
      transformer.transform(source,result);
      is = new ByteArrayInputStream(os.toByteArray());
    } catch (TransformerException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    return is;
  }
}

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

package com.servingxml.components.recordmapping;

import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.AbstractSaxSource;

/**
 * A <code>RecordSaxSource</code> implements SaxSource.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


public class RecordSaxSource extends AbstractSaxSource implements SaxSource {     

  private final XMLReader reader;
  private final Expirable expirable;
  private final Key key;
                                           
  public RecordSaxSource(XMLReader reader, Expirable expirable, Key key, TransformerFactory transformerFactory) {
    super(transformerFactory);

    this.reader = reader;
    this.expirable = expirable;
    this.key = key;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("RecordSaxSource:  ");

    return buf.toString();
  }

  public Expirable getExpirable() {
    return expirable;
  }                                       

  public XMLReader createXmlReader() {

    return reader;
  }

  public String getSystemId() {
    return "";
  }

  public Key getKey() {
    return key;
  }
}


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

package com.servingxml.components.error;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import com.servingxml.io.saxsource.AbstractSaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;

/**
 * A <code>ErrorSaxSource</code> instance may be used to obtain objects that
 * implement the <code>SAXSource</code> interface.
 * Objects that implements the TrAX <code>SAXSource</code> interface contain 
 * the information that is needed to supply XML content as SAX events.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ErrorSaxSource extends AbstractSaxSource {     

  private final ServingXmlException fault;
  private final Key key;

  public ErrorSaxSource(ServingXmlException fault, TransformerFactory transformerFactory) {
    super(transformerFactory);
    this.fault = fault;
    this.key = DefaultKey.newInstance();
  }

  public String getSystemId() {
    return key.toString();
  }

  public XMLReader createXmlReader() {

    XMLReader xmlReader = fault.createXmlReader();
    return xmlReader;
  }

  public final Key getKey() {
    return key;
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }
}

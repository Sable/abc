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

package com.servingxml.components.content; 

import java.util.Properties;

import org.xml.sax.XMLReader;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;

public class DocumentSequenceSaxSource implements SaxSource {
  private final Name documentElementName;
  private final String documentElementQname;
  private final Key key;
  private final Properties outputProperties = new Properties();
  private final ServiceContext context;
  private final Flow flow;
  private final RecordReaderFactory recordReaderFactory;
  private final Content contentFactory;

  public DocumentSequenceSaxSource(ServiceContext context, Flow flow,
                                   Name documentElementName,
                                   String documentElementQname,
                                   RecordReaderFactory recordReaderFactory, 
                                   Content contentFactory) {
    this.documentElementName = documentElementName;
    this.documentElementQname = documentElementQname;
    this.context = context;
    this.flow = flow;
    this.recordReaderFactory = recordReaderFactory;
    this.contentFactory = contentFactory;
    this.key = DefaultKey.newInstance();
  }


  public XMLReader createXmlReader() {
    return new DocumentSequenceReader(context, flow, 
                                      documentElementName,
                                      documentElementQname,
                                      recordReaderFactory, contentFactory);
  }
  
  public Key getKey() {
    return key;
  }
  
  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }
  
  public String getSystemId() {
    return "";
  }

  public Properties getDefaultOutputProperties() {
    return outputProperties;
  }
}
   

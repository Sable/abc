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

package com.servingxml.extensions.saxon.xquery;

import java.util.Properties;

import org.xml.sax.XMLReader;

import net.sf.saxon.s9api.XQueryExecutable;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.components.content.Content;
import com.servingxml.util.PrefixMap;

public class XQuerySaxSource implements SaxSource {
  private final PrefixMap prefixMap;
  private static ServiceContext context;
  private static Flow flow;
  private final String baseUri;
  private final Content contextDocument;
  private final Properties defaultOutputProperties = new Properties();
  private final XQueryExecutable exp;

  public XQuerySaxSource(PrefixMap prefixMap, ServiceContext context, Flow flow, String baseUri, 
                       Content contextDocument, XQueryExecutable exp) {
    this.prefixMap = prefixMap;
    this.context = context;
    this.flow = flow;
    this.baseUri = baseUri;
    this.contextDocument = contextDocument;
    this.exp = exp;
  }

  public XMLReader createXmlReader() {
    return new XQueryReader(prefixMap, context, flow, baseUri, contextDocument, exp);
  }

  public Key getKey() {
    return DefaultKey.newInstance();
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public String getSystemId() {
    return "";
  }

  public Properties getDefaultOutputProperties() {
    return defaultOutputProperties;
  }
}

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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.util.xml.DomSubtreeReader;
import java.util.Properties;
import javax.xml.transform.ErrorListener;
import org.w3c.dom.Document;
import org.xml.sax.XMLReader;
import com.servingxml.components.content.DefaultUriResolverFactory;
import com.servingxml.util.PrefixMap;

/**
 * Factory for creating a SaxSource. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DomSaxSourceFactory implements SaxSourceFactory {
  private final PrefixMap prefixMap;
  private final Key key;
  private final Expirable expirable;
  private final Document document;
  private final String baseUri;
  private final OutputPropertyFactory[] outputPropertyFactories;

  public DomSaxSourceFactory(Document document, PrefixMap prefixMap, String baseUri, OutputPropertyFactory[] outputPropertyFactories) {
    this.prefixMap = prefixMap;
    this.key = DefaultKey.newInstance();
    this.expirable = Expirable.NEVER_EXPIRES;
    this.document = document;
    this.baseUri = baseUri;
    this.outputPropertyFactories = outputPropertyFactories;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    XMLReader xmlReader = new DomSubtreeReader(document,
                                               prefixMap,
                                               baseUri,
                                               flow.getParameters(),
                                               context.getTransformerFactory(),
                                               context.getUriResolverFactory(), 
                                               context.getTransformerErrorListener());

    Properties properties = new Properties();
    for (int i = 0; i < outputPropertyFactories.length; ++i) {
      OutputProperty property = outputPropertyFactories[i].createOutputProperty(context,flow);
      properties.setProperty(property.getName(),property.getValue());
    }

    SaxSource saxSource = new XmlReaderSaxSource(xmlReader, key, expirable,
      properties, context.getTransformerFactory());

    return saxSource;
  }
}


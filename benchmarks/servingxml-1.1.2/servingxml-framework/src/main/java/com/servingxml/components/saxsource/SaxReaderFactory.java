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

import com.servingxml.app.ServiceContext;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.AbstractSaxSource;
import com.servingxml.util.InstanceFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.app.Flow;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;

/**
 * Factory for creating a SaxSource. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SaxReaderFactory implements SaxSourceFactory {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};
  private final static Expirable expirable = Expirable.IMMEDIATE_EXPIRY;
  private final InstanceFactory instanceFactory;
  private final Properties properties;
  private final OutputPropertyFactory[] defaultOutputPropertyFactories;
  private final SubstitutionExpr systemIdResolver;

  public SaxReaderFactory(InstanceFactory instanceFactory, Properties properties, 
                          OutputPropertyFactory[] defaultOutputPropertyFactories, SubstitutionExpr systemIdResolver) {
    this.instanceFactory = instanceFactory;
    this.properties = properties;
    this.defaultOutputPropertyFactories = defaultOutputPropertyFactories;
    this.systemIdResolver = systemIdResolver;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    XMLReader xmlReader;

    String systemId = systemIdResolver.evaluateAsString(flow.getParameters(), flow.getRecord());

    Properties defaultOutputProperties = new Properties();
    for (int i = 0; i < defaultOutputPropertyFactories.length; ++i) {
      OutputProperty property = defaultOutputPropertyFactories[i].createOutputProperty(context,flow);
      defaultOutputProperties.setProperty(property.getName(), property.getValue());
    }

    return new SaxReader(instanceFactory, properties, context, flow, systemId, expirable,
      defaultOutputProperties, context.getTransformerFactory());
  }
}

class SaxReader extends AbstractSaxSource {
  private static final Class[] CTOR_ARG_TYPES3 = new Class[]{Properties.class, ServiceContext.class, Flow.class};
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};
  private final InstanceFactory instanceFactory;
  private final Properties properties;
  private final String systemId;
  private final Key key;
  private final Expirable expirable;
  private final ServiceContext context; 
  private final Flow flow;

  public SaxReader(InstanceFactory instanceFactory, Properties properties, 
                      ServiceContext context, Flow flow,
                      String systemId, Expirable expirable, Properties outputProperties, TransformerFactory transformerFactory) {
    super(outputProperties, transformerFactory);
    this.instanceFactory = instanceFactory;
    this.properties = properties;
    this.systemId = systemId;
    this.key = DefaultKey.newInstance();
    this.expirable = expirable;
    this.context = context;
    this.flow = flow;
  }

  public XMLReader createXmlReader() {
    XMLReader xmlReader;
    if (instanceFactory.hasConstructor(CTOR_ARG_TYPES3)) {
      Object[] args = new Object[]{properties,context,flow};
      xmlReader = (XMLReader)instanceFactory.createInstance(CTOR_ARG_TYPES3,args);
    } else if (instanceFactory.hasConstructor(CTOR_ARG_TYPES)) {
      Object[] args = new Object[]{properties};
      xmlReader = (XMLReader)instanceFactory.createInstance(CTOR_ARG_TYPES,args);
    } else {
      xmlReader = (XMLReader)instanceFactory.createInstance();
    }
    return xmlReader;
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
}

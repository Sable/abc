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
import org.xml.sax.XMLFilter;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.saxsource.SaxEventBuffer;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.cache.Cache;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.RevalidationType;
import com.servingxml.app.Flow;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.io.saxsource.XmlReaderSaxSource;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                      

public class CachedContent extends AbstractContent implements Content {     

  private final Content contentFactory;
  private final RevalidationType revalidationType;
  private final Cache cache;

  public CachedContent(Content contentFactory, Cache cache, RevalidationType revalidationType,
    OutputPropertyFactory[] defaultOutputPropertyFactories) {
    super(defaultOutputPropertyFactories);

    this.contentFactory = contentFactory;
    this.revalidationType = revalidationType;
    this.cache = cache;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    XmlPipeline nonCachingPipeline = contentFactory.createXmlPipeline(context, flow);
    SaxSource saxSource = new XmlReaderSaxSource(nonCachingPipeline.getXmlReader(),
      nonCachingPipeline.getOutputProperties(), context.getTransformerFactory());
    SaxSource cachedSaxSource = new CachedSaxSource(saxSource, cache, revalidationType);
    return cachedSaxSource;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    XmlPipeline nonCachingPipeline = contentFactory.createXmlPipeline(context, flow);
    SaxSource saxSource = new XmlReaderSaxSource(nonCachingPipeline.getXmlReader(),
      nonCachingPipeline.getOutputProperties(), context.getTransformerFactory());
    pipeline.setSaxSource(saxSource);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline nonCachingPipeline = contentFactory.createXmlPipeline(context, flow);
    SaxSource saxSource = new XmlReaderSaxSource(nonCachingPipeline.getXmlReader(),
      nonCachingPipeline.getOutputProperties(), context.getTransformerFactory());
    SaxSource cachedSaxSource = new CachedSaxSource(saxSource, cache, revalidationType);
    XmlPipeline cachingPipeline = new XmlPipeline(/*defaultOutputProperties*/);
    cachingPipeline.setSaxSource(cachedSaxSource);
    return cachingPipeline;
  }
}

class CachedSaxSource implements SaxSource, CachedContentCommand {     

  private final SaxSource saxSource;
  private final Cache cache;
  private final RevalidationType revalidationType;
  private final Properties outputProperties = new Properties();

  public CachedSaxSource(SaxSource saxSource, Cache cache,
  RevalidationType revalidationType) {

    this.saxSource = saxSource;
    this.cache = cache;
    this.revalidationType = revalidationType;
  }

  public String getSystemId() {
    return saxSource.getSystemId();
  }

  public Key getKey() {
    return saxSource.getKey();
  }

  public Expirable getExpirable() {
    return saxSource.getExpirable();
  }

  public XMLReader createXmlReader() {

    XMLReader reader = null;
    Key key = getKey();
    SaxEventBuffer resource = (SaxEventBuffer)cache.get(key);
    if (resource == null) {
      reader = saxSource.createXmlReader();
      XMLFilter xmlFilter = new CachedContentFilter(this);
      xmlFilter.setParent(reader);
      reader = xmlFilter;
    } else {
      reader = resource.createXmlReader();
    }
    return reader;                                   
  }

  public void execute(SaxEventBuffer saxEventBuffer) {

    Expirable expirable = saxSource.getExpirable();
    if (!expirable.immediateExpiry()) {
      cache.add(getKey(),saxEventBuffer,expirable, revalidationType);
    }
  }

  public Properties getDefaultOutputProperties() {
    return outputProperties;
  }
}


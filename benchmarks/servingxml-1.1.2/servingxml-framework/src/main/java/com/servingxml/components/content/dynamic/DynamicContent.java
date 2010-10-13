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

package com.servingxml.components.content.dynamic;

import java.lang.reflect.Method;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLReader;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.AbstractContent;
import com.servingxml.util.Name;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.app.Flow;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.AbstractSaxSource;
import com.servingxml.components.content.Content;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;

/**
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DynamicContent extends AbstractContent implements Content{

  private final DynamicContentHandler requestHandler;
  private final Name documentName;
  private final KeyIdentifier keyIdentifier;
  private final Method handlerMethod;
  private final RecordMetaData recordMetaData;
  private final DynamicChangeable dynamicExpirable;

  DynamicContent(Name documentName, DynamicContentHandler requestHandler,
  Method handlerMethod, RecordMetaData recordMetaData,
  DynamicChangeable dynamicExpirable, KeyIdentifier keyIdentifier, OutputPropertyFactory[] defaultOutputProperties) {
    super(defaultOutputProperties);

    this.documentName = documentName;
    this.requestHandler = requestHandler;
    this.handlerMethod = handlerMethod;
    this.recordMetaData = recordMetaData;
    this.dynamicExpirable = dynamicExpirable;
    this.keyIdentifier = keyIdentifier;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    Object parametersProxy = recordMetaData.createRecordProxy(flow.getParameters());
    DynamicContentKey key = new DynamicContentKey(documentName, flow.getParameters(), keyIdentifier);
    Expirable expirable = new DynamicChangeableExpirable(dynamicExpirable,parametersProxy);
    SaxSource content = new DynamicSaxSource(context,parametersProxy,key,requestHandler,handlerMethod,expirable,
      context.getTransformerFactory());
    return content;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = new XmlPipeline(/*defaultOutputProperties*/);
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
    return pipeline;
  }
}

class DynamicSaxSource extends AbstractSaxSource implements SaxSource {

  private final ServiceContext context;
  private final Object parametersProxy;
  private final DynamicContentKey key;
  private final DynamicContentHandler requestHandler;
  private final Method handlerMethod;
  private final Expirable expirable;

  DynamicSaxSource(ServiceContext context, Object parametersProxy, DynamicContentKey key,
  DynamicContentHandler requestHandler, Method handlerMethod, Expirable expirable, TransformerFactory transformerFactory) {
    super(transformerFactory);

    this.context = context;
    this.parametersProxy = parametersProxy;
    this.key = key;
    this.requestHandler = requestHandler;
    this.handlerMethod = handlerMethod;
    this.expirable = expirable;
  }

  public XMLReader createXmlReader() {
    XMLReader xmlReader = new DynamicContentReader(requestHandler, handlerMethod,
      context, parametersProxy);
    return xmlReader;
  }

  public Expirable getExpirable() {
    return expirable;
  }

  public String getSystemId() {
    return "";
  }

  public Key getKey() {
    return key;
  }
}



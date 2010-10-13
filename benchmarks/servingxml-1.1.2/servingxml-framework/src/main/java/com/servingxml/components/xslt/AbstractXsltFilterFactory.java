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

package com.servingxml.components.xslt;

import java.util.Enumeration;

import javax.xml.transform.TransformerException;
import javax.xml.transform.Templates;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import java.net.URL;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.cache.Expirable;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.components.common.UrlEvaluator;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.content.Content;
import com.servingxml.components.parameter.WithParameters;
import com.servingxml.app.Environment;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class AbstractXsltFilterFactory extends AbstractXmlFilterAppender implements Content {

  private final Environment env;
  private final UrlEvaluator docBaseResolver;
  private final SaxSourceFactory saxSourceFactory;
  private final WithParameters withParameters;

  public AbstractXsltFilterFactory(Environment env,
                                   SaxSourceFactory saxSourceFactory, 
                                   UrlEvaluator docBaseResolver, 
                                   WithParameters withParameters) {

    this.env = env;
    this.docBaseResolver = docBaseResolver;
    this.saxSourceFactory = saxSourceFactory;
    this.withParameters = withParameters;
  }
                            
  public Templates getTemplates(ServiceContext context, SaxSource saxSource) {
    
    boolean good = false;
    try {
      TransformerFactory transformerFactory = context.getTransformerFactory();
      XMLReader xmlReader = saxSource.createXmlReader();
      Source templatesSource = new SAXSource(xmlReader,new InputSource(saxSource.getSystemId()));
      Templates templates = transformerFactory.newTemplates(templatesSource);
      good = true;
      return templates;
    } catch (TransformerException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
  XmlFilterChain pipeline) {
    Flow newFlow = env.augmentParametersOf(context,flow);

    SaxSource saxSource = saxSourceFactory.createSaxSource(context, newFlow);

    Expirable expirable = saxSource.getExpirable();
    Templates templates = getTemplates(context, saxSource);

    String documentBase = "";
    if (docBaseResolver != null) {
      URL url = docBaseResolver.evaluateUrl(newFlow.getParameters(),newFlow.getRecord());
      documentBase = url.toString();
    }

    XMLFilter filter = new XsltFilter(env,context, newFlow, templates, documentBase, withParameters);
    pipeline.addXmlFilter(filter);
    if (expirable != null) {
      pipeline.addExpirable(expirable); 
    }
    pipeline.addOutputProperties(templates.getOutputProperties());

    //Properties properties = pipeline.getOutputProperties();
    //Enumeration names = properties.propertyNames();
   //System.out.println(getClass().getName()+".appendToXmlPipeline");
    //while (names.hasMoreElements()) {
      //String prop = properties.getProperty((String)names.nextElement());
     //System.out.println(prop);
    //}

  }
}


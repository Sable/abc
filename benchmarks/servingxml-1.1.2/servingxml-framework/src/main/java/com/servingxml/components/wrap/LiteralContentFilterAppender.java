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

package com.servingxml.components.wrap;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.content.AbstractContent;
import com.servingxml.components.content.Content;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.string.Stringable;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.Selectable;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import com.servingxml.components.saxfilter.ContentXmlFilter;
import com.servingxml.io.saxsource.XmlReaderSaxSource;

/**
 * Implements a factory class for creating literal content
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LiteralContentFilterAppender extends AbstractContent implements Content {

  private final QnameContext nameContext;
  private final NameSubstitutionExpr nameResolver;
  private final AttributesImpl attributes;
  private final ElementAttributeFactory[] attributeFactories;
  private final Stringable stringFactory;
  private final Content[] xmlComponents;

  public LiteralContentFilterAppender(QnameContext nameContext, NameSubstitutionExpr nameResolver, 
                                      AttributesImpl attributes, Stringable stringFactory, ElementAttributeFactory[] attributeFactories,
                                      Content[] xmlComponents) {
    this.nameContext = nameContext;
    this.nameResolver = nameResolver;
    this.attributes = attributes;
    this.stringFactory = stringFactory;
    this.attributeFactories = attributeFactories;
    this.xmlComponents = xmlComponents;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                                  XmlFilterChain pipeline) {
    SaxSource saxSource = createSaxSource(context,flow);
    XMLFilter filter = new ContentXmlFilter(context,flow,saxSource);
    pipeline.addXmlFilter(filter);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".createXmlPipeline");
    XmlPipeline pipeline = new XmlPipeline(createDefaultOutputProperties(context,flow));
    //SaxSource saxSource = flow.getDefaultSaxSource();
    //pipeline.setSaxSource(saxSource);
    appendToXmlPipeline(context, flow, pipeline);
    return pipeline;
  }

  public XMLReader createXmlReader(ServiceContext context, Flow flow) {
    AttributesImpl atts = new AttributesImpl();
    for (int i = 0; i < attributes.getLength();++i) {
      atts.addAttribute(attributes.getURI(i),attributes.getLocalName(i),attributes.getQName(i),
                        attributes.getType(i),attributes.getValue(i));
    }
    Record variables = Record.EMPTY;

    ElementAttribute[] elementAttributes = new ElementAttribute[attributeFactories.length];

    for (int i = 0; i < attributeFactories.length; ++i) {
      elementAttributes[i] = attributeFactories[i].createElementAttribute();
    }

    for (int i = 0; i < elementAttributes.length; ++i) {
      elementAttributes[i].addToAttributes(context,flow,atts);
    }

    XMLReader[] children = new XMLReader[xmlComponents.length];
    for (int i = 0; i < xmlComponents.length; ++i) {
      Content xmlFilterAppender = xmlComponents[i];
      children[i] = xmlFilterAppender.createSaxSource(context,flow).createXmlReader(); 
    }
    XMLReader reader = new InlineContentReader(nameContext, context,flow,nameResolver,atts,stringFactory,
                                               children);
    return reader;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    XMLReader reader = createXmlReader(context,flow);
    SaxSource saxSource = new XmlReaderSaxSource(reader,
                                                 createDefaultOutputProperties(context,flow), 
                                                 context.getTransformerFactory());
    return saxSource;
  }
}



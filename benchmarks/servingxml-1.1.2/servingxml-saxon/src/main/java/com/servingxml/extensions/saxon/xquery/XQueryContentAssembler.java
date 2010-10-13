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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.DefaultDocument;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.components.streamsource.string.StringStreamSourceFactory;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>XQueryContentAssembler</code> implements an assembler for
 * assembling system <code>Content</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XQueryContentAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private StreamSourceFactory queryFactory = null;
  private StringFactory stringQueryFactory = null;
  private XsltConfiguration xsltConfiguration;
  private Content contextDocument = null;
  private String baseUri = null;

  public void setDocumentBase(String baseUri) {
    this.baseUri = baseUri;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(StreamSourceFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  public void injectComponent(Content contextDocument) {
    this.contextDocument = contextDocument;
  }

  public void injectComponent(StringFactory stringQueryFactory) {
    this.stringQueryFactory = stringQueryFactory;
  }

  public Content assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (queryFactory == null && stringQueryFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:stringable or sx:streamSource");
      throw new ServingXmlException(message);
    }

    if (queryFactory == null) {
      ValueEvaluator valueEvaluator = new StringValueEvaluator(stringQueryFactory);
      queryFactory = new StringStreamSourceFactory(valueEvaluator);
    }
    if (contextDocument == null) {
      contextDocument = new DefaultDocument(xsltConfiguration.getOutputPropertyFactories());
    }
    if (baseUri == null) {
      baseUri = context.getQnameContext().getBase();
    }

    PrefixMap prefixMap = DomHelper.createPrefixMap(context.getElement());
      
    Content contentFactory = new XQueryContent(baseUri, prefixMap, queryFactory, contextDocument,
                                                             xsltConfiguration.getOutputPropertyFactories());
    if (parameterDescriptors.length > 0) {
      contentFactory = new ContentPrefilter(contentFactory,parameterDescriptors);
    }
    return contentFactory;
  }
}

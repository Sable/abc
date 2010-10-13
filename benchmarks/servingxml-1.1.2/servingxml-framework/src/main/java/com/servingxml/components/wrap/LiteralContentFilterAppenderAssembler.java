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

import java.util.Comparator;

import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.SimpleNameEvaluator;
import com.servingxml.components.string.StringLiteralFactory;
import com.servingxml.components.string.Stringable;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.PrefixMap;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * The <code>LiteralContentFilterAppenderAssembler</code> implements an assembler for
 * assembling <code>LiteralContent</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LiteralContentFilterAppenderAssembler {
  
  private Content[] xmlComponents = new Content[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private ElementAttributeFactory[] attributeFactories = new ElementAttributeFactory[0];

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public void injectComponent(ElementAttributeFactory[] attributeFactories) {
    this.attributeFactories = attributeFactories;
  }
  
  public Content assemble(ConfigurationContext context) {

    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }

      String namespaceUri;
      String localName;
      String qname;
      final AttributesImpl attributes = new AttributesImpl();

      Element element = context.getElement();
      String ns = element.getNamespaceURI();
      if (ns == null) {
        ns = "";
      }
      namespaceUri = ns;                                   
      localName = element.getLocalName();
      qname = element.getTagName();

      StringFactory stringFactory = StringFactoryCompiler.fromStringFactories(context, context.getElement());

      PrefixMap prefixMap = context.getQnameContext().getPrefixMap();
      NamedNodeMap attributeList = element.getAttributes();
      for (int i = 0; i < attributeList.getLength(); ++i) {
        Node attr = attributeList.item(i);
        String uri = attr.getNamespaceURI();
        if (uri == null) {
          uri = "";
        }
        String attributeQname = attr.getNodeName();
        if (!attributeQname.startsWith("xmlns")) {
          attributes.addAttribute(uri,attr.getLocalName(),attributeQname,"CDATA",
                                  attr.getNodeValue());
          //System.out.println("LiteralContent attribute {" + uri + "}" + attr.getLocalName() + " " + attributeQname);
        }
      }

      //String prefix = context.getQnameContext().getPrefix(namespaceUri);
      NameSubstitutionExpr nameResolver = new SimpleNameEvaluator(new QualifiedName(namespaceUri, localName));

      Content filterFactory = new LiteralContentFilterAppender(
        context.getQnameContext(), nameResolver, attributes, stringFactory, attributeFactories, xmlComponents);
      if (parameterDescriptors.length > 0) {
        filterFactory = new ContentPrefilter(filterFactory,parameterDescriptors);
      }

      return filterFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }

  }
}


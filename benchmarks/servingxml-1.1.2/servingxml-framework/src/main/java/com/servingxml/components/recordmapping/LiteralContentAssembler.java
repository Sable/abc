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

package com.servingxml.components.recordmapping;

import java.util.Comparator;

import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.SimpleNameEvaluator;
import com.servingxml.components.string.StringLiteralFactory;
import com.servingxml.components.string.Stringable;
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
import com.servingxml.app.Environment;

/**
 * The <code>LiteralContentAssembler</code> implements an assembler for
 * assembling <code>LiteralContent</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LiteralContentAssembler {
  
  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private Sort[] sorts = new Sort[0];

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Sort[] sorts) {

    this.sorts = sorts;
  }
  
  public void injectComponent(MapXmlFactory[] childFactories) {
    this.childFactories = childFactories;
  }
  
  public MapXmlFactory assemble(ConfigurationContext context) {
    //System.out.println(getClass().getName()+".assemble");

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

      Stringable stringFactory = StringFactoryCompiler.fromStringFactories(context, context.getElement());

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

      String prefix = DomHelper.getPrefix(qname);
      NameSubstitutionExpr nameResolver = new SimpleNameEvaluator(new QualifiedName(namespaceUri,localName));

      MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);
      if (sorts.length > 0) {
        Comparator comparator = new SortComparator(sorts);
        rmf = new SortGroupFactory(rmf,comparator);
      }
      //for (int i = 0; i < parameterDescriptors.length; ++i) {
      //System.out.println(getClass().getName()+".assemble param="+parameterDescriptors[i].getName());
      //}
      // 
      Environment env = new Environment(parameterDescriptors,context.getQnameContext());

      PrefixMap  localPrefixMap = context.getQnameContext().getPrefixMap();
      MapXmlFactory recordMapFactory = new LiteralContent(env,nameResolver ,localPrefixMap,attributes,stringFactory,rmf);

      return recordMapFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }

  }
}


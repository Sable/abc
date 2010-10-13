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

import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.util.xml.XPathExpressionFactory;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.XPathEvaluator;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.common.StringLiteralValueEvaluator;
import com.servingxml.expr.substitution.FieldSubstitutor;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.content.Content;

/**
 * The <code>ElementAttributeFactoryAssembler</code> implements an assembler for
 * assembling <code>ElementAttribute</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ElementAttributeFactoryAssembler {

  private String attributeQname = null;
  private String value = null;
  private String select = null;
  private Content content = null;
  private XsltConfiguration xsltConfiguration;

  public void setSelect(String select) {
    this.select = select;
  }

  public void setName(String qname) {
    this.attributeQname = qname;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(Content content) {
    this.content = content;
  }

  public ElementAttributeFactory assemble(ConfigurationContext context) {

    if (attributeQname == null || attributeQname.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"attribute");
      throw new ServingXmlException(message);
    }

    NameSubstitutionExpr nameResolver = NameSubstitutionExpr.parse(context.getQnameContext(),attributeQname);
    ValueEvaluator valueEvaluator = null;

    if (value != null) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
      valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
    } else if (select != null) {
      XPathExpressionFactory exprFactory = new XPathExpressionFactory(context.getQnameContext(),select,xsltConfiguration.getVersion(),
        xsltConfiguration.getTransformerFactory());
      if (content == null) {
        valueEvaluator = new XPathEvaluator(exprFactory);
      } else {
        valueEvaluator = new XPathEvaluator(exprFactory, content);
      }
    } else {
      StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
      valueEvaluator = new StringValueEvaluator(stringFactory);
    }

    ElementAttributeFactory factory = new ElementAttributeFactory(context.getQnameContext(), nameResolver, valueEvaluator);
    return factory;
  }
}

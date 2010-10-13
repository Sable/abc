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

package com.servingxml.components.recordio;

import org.w3c.dom.Element;

import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.XPathEvaluator;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.expr.substitution.LiteralSubstitutionExpr;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.XPathExpressionFactory;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.content.Content;

/**
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class NewFieldAssembler {

  private Name fieldName = Name.EMPTY;
  private String value = null;
  private String selectExpr = "";
  private Content content = null;
  private XsltConfiguration xsltConfiguration;

  public void setName(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setSelect(String selectExpr) {
    this.selectExpr = selectExpr;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(Content content) {
    this.content = content;
  }

  public NewField assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (fieldName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
                                                                 "name");
      throw new ServingXmlException(message);
    }

    ValueEvaluator valueEvaluator = null;
    if (selectExpr.length() > 0) {
      XPathExpressionFactory exprFactory = new XPathExpressionFactory(context.getQnameContext(),selectExpr,xsltConfiguration.getVersion(),
                                                                      xsltConfiguration.getTransformerFactory());
      if (content == null) {
        valueEvaluator = new XPathEvaluator(exprFactory);
      } else {
        valueEvaluator = new XPathEvaluator(exprFactory, content);
      }
    } else if (value != null) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
      valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
    } else {
      StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
      valueEvaluator = new StringValueEvaluator(stringFactory);
    }
    NewField newField = new NewFieldImpl(fieldName, valueEvaluator);

    return newField;
  }
}



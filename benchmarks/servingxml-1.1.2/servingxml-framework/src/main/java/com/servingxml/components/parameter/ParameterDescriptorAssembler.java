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

package com.servingxml.components.parameter;

import org.w3c.dom.Element;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.XPathEvaluator;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.DefaultDocument;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.ValueType;
import com.servingxml.util.record.ValueTypeFactory;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.XPathExpressionFactory;

/**
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ParameterDescriptorAssembler {

  private Name parameterName = Name.EMPTY;
  private String value = null;
  private String selectExpr = "";
  private Content content = null;
  private DefaultValue defaultValue = null;
  private XsltConfiguration xsltConfiguration;
  private Name typeName = ValueTypeFactory.STRING_TYPE_NAME;

  public void setName(Name parameterName) {
    this.parameterName = parameterName;
  }

  public void setType(Name typeName) {
    this.typeName = typeName;
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

  public void injectComponent(DefaultValue defaultValue) {
    this.defaultValue = defaultValue;
  }

  public ParameterDescriptor assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (parameterName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"name");
      throw new ServingXmlException(message);
    }

    ValueType valueType = ValueTypeFactory.lookupValueType(typeName);
    if (valueType == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,context.getElement().getTagName(),"type");
      throw new ServingXmlException(message);
    }

    ParameterDescriptor parameterDescriptor;
    if (defaultValue != null) {
      ValueEvaluator valueEvaluator = new DefaultParameterValueEvaluator(parameterName, defaultValue.getValueEvaluator());
      parameterDescriptor = new ParameterDescriptorImpl(parameterName, valueEvaluator, valueType);
    } else {
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
        //System.out.println(getClass().getName()+".assemble Compiling String");
        StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
        valueEvaluator = new StringValueEvaluator(stringFactory);
      }
      parameterDescriptor = new ParameterDescriptorImpl(parameterName, valueEvaluator, valueType);
    }

    return parameterDescriptor;
  }
}



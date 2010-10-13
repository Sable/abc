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

package com.servingxml.components.saxfilter;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.expr.saxpath.RestrictedMatchParser;

public class AssertAppenderAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String test = "";

  public void setTest(String test) {
    this.test = test;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public Content assemble(ConfigurationContext context) {

    if (test.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
                                                                 "test");
      throw new ServingXmlException(message);
    }

    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }

      String baseUri = context.getQnameContext().getBase();
      XPathBooleanExpressionFactory testExprFactory = new XPathBooleanExpressionFactoryImpl(context.getQnameContext(), 
                                                                                            test, 
                                                                                            xsltConfiguration.getVersion(),
                                                                                            baseUri,
                                                                                            xsltConfiguration.getTransformerFactory());
      StringFactory messageFactory = StringFactoryCompiler.fromStringables(context, context.getElement());

      Content content = new AssertAppender(testExprFactory, messageFactory);
      if (parameterDescriptors.length > 0) {
        content = new ContentPrefilter(content,parameterDescriptors);
      }
      return content;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
                                                                 context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}



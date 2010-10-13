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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.SimpleXPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;
import com.servingxml.app.Environment;

/**
 * The <code>InnerGroupFactoryAssembler</code> implements an assembler for
 * assembling <code>InnerGroupFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class InnerGroupFactoryAssembler {
  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private Sort[] sorts = new Sort[0];
  private String startTest = "";
  private String endTest = "";

  public void setStartTest(String startTest) {
    this.startTest = startTest;
  }

  public void setEndTest(String endTest) {
    this.endTest = endTest;
  }

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

     if (childFactories.length == 0) {
       String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_CHOICE_REQUIRED,
                                                              context.getElement().getTagName(),
                                                              "literal, sx:fieldElementMap, sx:group, sx:onRecord");
       throw new ServingXmlException(msg);
     }

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    try {
      MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);

      if (sorts.length > 0) {
        Comparator comparator = new SortComparator(sorts);
        rmf = new SortGroupFactory(rmf,comparator);
      }
      String baseUri = context.getQnameContext().getBase();
      XPathBooleanExpressionFactory startTestFactory;
      if (startTest.length() > 0) {
        startTestFactory = new XPathBooleanExpressionFactoryImpl(
          context.getQnameContext(), startTest, xsltConfiguration.getVersion(),
        baseUri, xsltConfiguration.getTransformerFactory());
      } else {
        startTestFactory = new SimpleXPathBooleanExpressionFactory(XPathBooleanExpression.ALWAYS_TRUE);
      }
      XPathBooleanExpressionFactory endTestFactory;
      if (endTest.length() > 0) {
        endTestFactory = new XPathBooleanExpressionFactoryImpl(
          context.getQnameContext(), endTest, xsltConfiguration.getVersion(),
        baseUri, xsltConfiguration.getTransformerFactory());
      } else {
        endTestFactory = startTestFactory;
      }

      Environment env = new Environment(parameterDescriptors,context.getQnameContext());

      GroupRecognizer groupRecognizer = new GroupRecognizerImpl(env.getQnameContext().getPrefixMap(), startTestFactory, endTestFactory);
      MapXmlFactory recordMapFactory = new InnerGroupFactory(env,groupRecognizer,rmf);

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


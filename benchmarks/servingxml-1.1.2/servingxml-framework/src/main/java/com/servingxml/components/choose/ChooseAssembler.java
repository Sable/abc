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

package com.servingxml.components.choose;

import javax.xml.transform.TransformerFactory;

import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.error.CatchError;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.xml.XsltChooserFactory;
import com.servingxml.util.xml.XsltChooser;

/**
 * Assembler for <code>sx:choose</code> component.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Choose
 */

public class ChooseAssembler {

  private XsltConfiguration xsltConfiguration;
  private Alternative[] alternatives = new Alternative[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private CatchError catchError = null;
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

  public void injectComponent(CatchError catchError) {
    this.catchError = catchError;
  }

  public void injectComponent(Alternative[] alternatives) {
    this.alternatives = alternatives;
  }

  public Choose assemble(final ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (alternatives.length == 0) {
      return null;
    }

    try {
      String[] tests = new String[alternatives.length];
      for (int i = 0; i < alternatives.length; ++i) {
        tests[i] = alternatives[i].getTest();
      }
      TransformerFactory transformerFactory = xsltConfiguration.getTransformerFactory();

      if (baseUri == null) {
        baseUri = context.getQnameContext().getBase();
      }

      XsltChooserFactory chooserFactory = new XsltChooserFactory(transformerFactory, baseUri,
                                                                 tests, 
                                                                 context.getQnameContext().getPrefixMap(), 
                                                                 xsltConfiguration.getVersion());

      Choose choose = new ChooseImpl(alternatives, chooserFactory);
      if (parameterDescriptors.length > 0) {
        choose = new ChoosePrefilter(choose,parameterDescriptors);
      }
      return choose;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
                         context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}



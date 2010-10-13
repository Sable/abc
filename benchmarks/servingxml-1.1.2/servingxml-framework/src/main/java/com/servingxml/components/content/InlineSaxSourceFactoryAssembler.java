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

package com.servingxml.components.content;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.saxsource.DomSaxSourceFactory;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.app.Environment;

/**
 * The <code>InlineSaxSourceFactoryAssembler</code> implements an assembler for
 * assembling system <code>SaxSourceFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class InlineSaxSourceFactoryAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String baseUri = null;

  public void setDocumentBase(String baseUri) {
    this.baseUri = baseUri;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public SaxSourceFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (baseUri == null) {
      baseUri = context.getQnameContext().getBase();
    }

    SaxSourceFactory saxSourceFactory = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // Turn off validation, and turn on namespaces
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      Node node = document.importNode(context.getElement(),true);
      document.appendChild(node);
      saxSourceFactory = new DomSaxSourceFactory(document, 
                                                 context.getQnameContext().getPrefixMap(),
                                                 baseUri, 
                                                 xsltConfiguration.getOutputPropertyFactories());
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    return saxSourceFactory;
  }
}

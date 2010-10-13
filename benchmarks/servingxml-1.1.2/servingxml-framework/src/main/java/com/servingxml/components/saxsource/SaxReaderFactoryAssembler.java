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

package com.servingxml.components.saxsource;

import java.util.Properties;

import org.xml.sax.XMLReader;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.property.Property;
import com.servingxml.util.InstanceFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.components.xsltconfig.XsltConfiguration;

public class SaxReaderFactoryAssembler {
  private XsltConfiguration xsltConfiguration;
  
  private Class javaClass = null;
  private String systemId = "";
  private Property[] customProperties = new Property[0];


  public void setClass(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(Property[] customProperties) {
    this.customProperties = customProperties;
  }

  public SaxSourceFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (javaClass == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"class");
      throw new ServingXmlException(message);
    }

    Properties properties = Property.toProperties(customProperties);

    InstanceFactory instanceFactory = new InstanceFactory(javaClass,XMLReader.class);
    SubstitutionExpr systemIdResolver = SubstitutionExpr.parseString(context.getQnameContext(),systemId);

    SaxSourceFactory saxSourceFactory = new SaxReaderFactory(instanceFactory,properties,
      xsltConfiguration.getOutputPropertyFactories(), systemIdResolver);
    return saxSourceFactory;
  }
}


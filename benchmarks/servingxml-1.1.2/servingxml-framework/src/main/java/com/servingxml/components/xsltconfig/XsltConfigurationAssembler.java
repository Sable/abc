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

package com.servingxml.components.xsltconfig;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.property.SystemProperty;
import com.servingxml.components.property.OutputPropertyFactory;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XsltConfigurationAssembler {

  private String version = "1.0";
  private OutputPropertyFactory[] outputPropertyFactories = new OutputPropertyFactory[0];
  private SystemProperty[] systemProperties = new SystemProperty[0];

  public void setVersion(String version) {
    this.version = version;
  }

  public void injectComponent(OutputPropertyFactory[] outputPropertyFactories) {
    this.outputPropertyFactories = outputPropertyFactories;
  }

  public void injectComponent(SystemProperty[] systemProperties) {
    this.systemProperties = systemProperties;
  }
  
  public XsltConfiguration assemble(ConfigurationContext context) {

    try {
      for (int i = 0; i < systemProperties.length; ++i) {
        SystemProperty property = systemProperties[i];
        if (property.getName().length() == 0) {
          String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"key");
          throw new ServingXmlException(message);
        }
        System.setProperty(property.getName(),property.getValue());
      }
      //for (int i = 0; i < outputPropertyFactories.length; ++i) {
        //OutputPropertyFactory property = outputPropertyFactories[i];
        //System.out.println(getClass().getName()+".assemble " + property.getName() + "=" + property.getValue());
      //}
      SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();

      return new XsltConfiguration(version, outputPropertyFactories, transformerFactory);
    } catch (TransformerFactoryConfigurationError e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}



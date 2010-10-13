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
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.TransformerFactory;
import com.servingxml.components.property.OutputPropertyFactory;

import com.servingxml.util.ServingXmlException;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XsltConfiguration {

  private final String version;
  private final OutputPropertyFactory[] outputPropertyFactories;
  private final SAXTransformerFactory transformerFactory;

  public XsltConfiguration(String version, OutputPropertyFactory[] outputPropertyFactories, SAXTransformerFactory transformerFactory) {
    this.version = version;
    this.outputPropertyFactories = outputPropertyFactories;
    this.transformerFactory = transformerFactory;
  }

  public String getVersion() {
    return version;
  }

  public OutputPropertyFactory[] getOutputPropertyFactories() {
    return outputPropertyFactories;
  }

  public SAXTransformerFactory getTransformerFactory() {
    return transformerFactory;
  }

  public static XsltConfiguration getDefault() { 
    try {
      SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
      return new XsltConfiguration("2.0", new OutputPropertyFactory[0],transformerFactory);  
    } catch (TransformerFactoryConfigurationError e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}


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

package com.servingxml.io.saxsource;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.InputSource;

import com.servingxml.util.ServingXmlException;

/**
 * Provides default implementation for a SaxSource
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


public abstract class AbstractSaxSource implements SaxSource {
  private final TransformerFactory transformerFactory;
  private final Properties outputProperties;

  protected AbstractSaxSource(TransformerFactory transformerFactory) {
    this.outputProperties = new Properties();
    this.transformerFactory = transformerFactory;
  }

  protected AbstractSaxSource(Properties outputProperties, TransformerFactory transformerFactory) {
    this.outputProperties = outputProperties;
    this.transformerFactory = transformerFactory;
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    try {
      SAXSource source = new SAXSource(createXmlReader(),new InputSource(""));
      Transformer transformer = transformerFactory.newTransformer();
      Result result = new StreamResult(writer);
      transformer.transform(source,result);
      return writer.toString();
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public Properties getDefaultOutputProperties() {
    return outputProperties;
  }
}

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

package com.servingxml.ioc.components;

import java.util.logging.Level;

import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.ioc.resources.ResourceTable;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.RuntimeContext;

/**
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface ConfigurationContext extends RuntimeContext {

  Document getDocument();
                             
  Element getElement();

  String getNamespaceUri(Element element);

  ConfigurationContext createInstance(Element element, 
    ComponentDictionary localComponentDictionary);

  ConfigurationContext createInstance(Element element);

  MutableNameTable getNameTable();

  void trace(String sourceClass, String sourceMethod, 
    String message, Level level);

  Object getConfigurationComponent(Class propertyType);

  Object getServiceComponent(Element element);

  SAXTransformerFactory getTransformerFactory();

  ConfigurationContext getParent();
 
  QnameContext getQnameContext();

  ResourceTable getResourceTable();

  ComponentDictionary getComponentDictionary();

  Record getParameters();
}



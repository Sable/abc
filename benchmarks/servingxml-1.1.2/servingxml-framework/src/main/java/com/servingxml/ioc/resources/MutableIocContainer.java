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

package com.servingxml.ioc.resources;

import java.net.URL;
import org.w3c.dom.Document;

import com.servingxml.util.record.Record;

public interface MutableIocContainer extends IocContainer {

  /**
   * Registers a service component instance associated with the specified java interface and name.
   *
   * @param javaInterface the java interface that the component implements.
   * @param uri the component instance uri
   * @param value a service component instance if found, null otherwise.
   */

  void registerServiceComponent(Class javaInterface, String uri, Object value);

  /**
   * Registes a configuration component instance associated with the specified java interface.
   *
   * @param javaInterface the java interface that the component implements.
   * @param value a configuration component instance.
   */

  void registerConfigurationComponent(Class javaInterface, Object value);

  void loadResources(URL resourcesUrl, Record parameters);

  void loadResources(Document resourcesDocument, String systemId, Record parameters);
}


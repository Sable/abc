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

import java.io.PrintStream;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.NameTable;
import com.servingxml.util.MutableNameTable;
import com.servingxml.ioc.components.ServiceComponent;
import com.servingxml.ioc.components.ConfigurationComponent;
import com.servingxml.ioc.components.ConfigurationContext;

public interface MutableResourceTable extends ResourceTable {
  void addServiceComponent(ConfigurationContext context, ServiceComponent component, String uri);

  void setApplicationComponent(ConfigurationContext context, ServiceComponent component); 

  void addConfigurationComponent(ConfigurationContext context, ConfigurationComponent component);
  
  void printDiagnostics(PrintStream printStream, NameTable nameTable);

  void createIocContainer(MutableNameTable nameTable, MutableIocContainer container);
}
                                                        

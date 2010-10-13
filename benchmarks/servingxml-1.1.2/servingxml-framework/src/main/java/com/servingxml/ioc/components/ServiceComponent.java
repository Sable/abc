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

import java.util.Map;

import com.servingxml.util.Name;
import com.servingxml.util.NameTable;

public class ServiceComponent implements Component {
  private final Name name;
  private final int nameSymbol;
  private final ComponentInterface interfaceDescriptor;
  private final ComponentTable componentTable;
  private final ComponentAssembler componentAssembler;

  public ServiceComponent(Name name, int nameSymbol, ComponentAssembler componentAssembler, 
    ComponentInterface interfaceDescriptor, ComponentTable componentTable) {
    this.componentAssembler = componentAssembler;
    this.name = name;
    this.nameSymbol = nameSymbol;
    this.componentTable = componentTable;
    this.interfaceDescriptor = interfaceDescriptor;
  }

  public Name getName() {
    return name;
  }

  public ComponentAssembler getComponentAssembler() {
    return componentAssembler;
  }

  public int getNameSymbol() {
    return nameSymbol;
  }

  //public Class getInterface(int nameSymbol) {
  //  Class javaInterface = componentTable.getInterface(nameSymbol);
  //  return javaInterface;
  //}

  public void initialize(NameTable nameTable, ComponentTable parent, Map<Integer,Class> serviceInterfaceMap) {
    interfaceDescriptor.registerInterface(nameTable, serviceInterfaceMap);
    if (componentTable != null) {
      componentTable.initialize(nameTable, parent,serviceInterfaceMap);
    }
  }

  public ComponentDictionary getChildComponents() {
    return componentTable;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append(" - ");
    sb.append(componentAssembler.getType().getName());
    sb.append("\n");
     //+ " " + assemblerClass.getName();
    if (!componentTable.isEmpty()) {
      sb.append("\n[" + componentTable.toString() + "]");
    }
    return sb.toString();
  }

  public Class[] getInterfaces() {
    return interfaceDescriptor.getInterfaces();
  }
}

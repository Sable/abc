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

import com.servingxml.util.Name;

public class ConfigurationComponent implements Component {
  private final Name name;
  private final int nameSymbol;
  private final ComponentAssembler componentAssembler;

  public ConfigurationComponent(Name name, int nameSymbol, ComponentAssembler componentAssembler) {
    this.name = name;
    this.nameSymbol = nameSymbol;
    this.componentAssembler = componentAssembler;
  }

  public Name getName() {
    return name;
  }

  public int getNameSymbol() {
    return nameSymbol;
  }

  public Class getInterface() {
    return componentAssembler.getType();
  }

  public ComponentAssembler getComponentAssembler() {
    return componentAssembler;
  }

  public String toString() {
    return name.toString(); // + " " + assemblerClass.getName();
  }
}

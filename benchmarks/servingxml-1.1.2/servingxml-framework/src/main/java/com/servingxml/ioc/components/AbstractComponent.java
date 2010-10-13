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

public class AbstractComponent implements Component {
  private final Name name;
  private final int nameSymbol;
  private final Class javaInterface;
  private final ComponentTable componentTable;

  public AbstractComponent(Name name, int nameSymbol, Class javaInterface, ComponentTable componentTable) {
    this.name = name;
    this.nameSymbol = nameSymbol;
    this.javaInterface = javaInterface;
    this.componentTable = componentTable;
  }

  public Name getName() {
    return name;
  }

  public int getNameSymbol() {
    return nameSymbol;
  }

  public Class getInterface() {
    return javaInterface;
  }

  public String toString() {
    String s = name.toString() + " " + javaInterface.getName();
    if (!componentTable.isEmpty()) {
      s += "\n[" + componentTable.toString() + "]";
    }
    return s;
  }
}

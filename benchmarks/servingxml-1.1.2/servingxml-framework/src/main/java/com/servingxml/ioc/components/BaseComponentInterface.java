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

// JAXP
import com.servingxml.util.xml.DomQnameContext;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;     
import com.servingxml.util.NameTable;

class BaseComponentInterface implements ComponentInterface {
  private final String name;
  private final int[] baseSymbols;
  private final Class type;
  private Class[] javaInterfaces = new Class[0];

  BaseComponentInterface(Class type, int[] baseSymbols) {
    this.name = "";
    this.baseSymbols = baseSymbols;
    this.type = type;
  }

  public int[] getKeys() {
    return baseSymbols;
  }

  public void registerInterface(NameTable nameTable, Map<Integer,Class> serviceInterfaceMap) {

    boolean typeFound = false;
    Class[] interfaces = new Class[baseSymbols.length];
    for (int i = 0; i < baseSymbols.length; ++i) {
      int baseSymbol = baseSymbols[i];
      Class javaInterface = serviceInterfaceMap.get(baseSymbol);
      if (javaInterface == null) {
        Name baseName = nameTable.lookupName(baseSymbol);
        if (baseName != null) {
          throw new ServingXmlException("Cannot find abstract component " + baseName);
        } else {
          throw new ServingXmlException("Cannot find abstract component " + baseSymbol);
        }
      }
      interfaces[i] = javaInterface;
      if (javaInterface.equals(type)) {
        typeFound = true;
      }
    }
    if (!typeFound) {
      javaInterfaces= new Class[interfaces.length+1];
      System.arraycopy(interfaces,0,javaInterfaces,0,interfaces.length);
      javaInterfaces[interfaces.length] = type;
    } else {
      javaInterfaces = interfaces;
    }
  }

  public Class[] getInterfaces() {
    return javaInterfaces;
  }
}



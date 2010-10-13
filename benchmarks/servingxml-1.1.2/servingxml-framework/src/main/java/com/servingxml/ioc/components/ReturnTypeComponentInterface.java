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
import com.servingxml.util.NameTable;

class ReturnTypeComponentInterface implements ComponentInterface {
  private static final int[] baseSymbols = new int[0];
  private final Class[] javaInterfaces;

  ReturnTypeComponentInterface(Class javaInterface) {
    this.javaInterfaces = new Class[]{javaInterface};
  }

  public void registerInterface(NameTable nameTable, Map<Integer,Class> serviceInterfaceMap) {
  }

  public Class[] getInterfaces() {
    return javaInterfaces;
  }

  public int[] getKeys() {
    return baseSymbols;
  }
}


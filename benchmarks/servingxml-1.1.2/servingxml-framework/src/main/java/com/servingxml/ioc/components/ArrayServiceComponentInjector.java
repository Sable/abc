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

import java.lang.reflect.Method;

import com.servingxml.util.ServingXmlException;        
import com.servingxml.ioc.components.ConfigurationContext;

public class ArrayServiceComponentInjector implements ComponentInjector {
  private final Method setter; 
  private final Object[] resources;

  public ArrayServiceComponentInjector(Method setter, Object[] resources) {
    this.setter = setter;
    this.resources = resources;
  }

  public void injectComponent(ConfigurationContext context, 
  Object componentAssembler) {
    try {
      Object[] args = new Object[]{resources};
      setter.invoke(componentAssembler, args);
    } catch (java.lang.IllegalAccessException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (java.lang.reflect.InvocationTargetException e) {
      ServingXmlException sxe = ServingXmlException.fromInvocationTargetException(e);
      throw sxe;
    }
  }
}

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

import com.servingxml.ioc.components.ComponentAssembler;
import com.servingxml.ioc.components.ConfigurationContext;

public class LazyComponentAccessor {
  private ComponentAssembler componentAssembler = null;
  private ConfigurationContext context = null;
  private Object componentInstance = null;

  LazyComponentAccessor(ComponentAssembler componentAssembler, 
  ConfigurationContext context) {
    this.componentAssembler = componentAssembler;
    this.context = context;
  }

  public boolean isInitialized() {
    return componentInstance != null;
  }

  public Object getComponent() {

    //  We evaluate the component lazily
    if (componentInstance == null) {
      if (componentAssembler != null) {
        //System.out.println(getClass().getName()+" type is " + componentAssembler.getType().getName());
        this.componentInstance = componentAssembler.assemble(context);
        this.componentAssembler = null;
        this.context = null;
      }
    }
    
    return componentInstance;
  }
}


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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.servingxml.ioc.components.ConfigurationComponent;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.ioc.components.ServiceComponent;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTable;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.Application;

public class ResourceTableImpl implements MutableResourceTable {

  private final HashMap<ResourceKey,LazyComponentAccessor> serviceComponentAccessorMap;
  private final ResourceTable parent;
  private final Map<Class,LazyComponentAccessor> configurationComponentAccessorMap;
  private LazyComponentAccessor appComponentAccessor;

  public ResourceTableImpl() {
    this.serviceComponentAccessorMap = new HashMap<ResourceKey,LazyComponentAccessor>();
    this.configurationComponentAccessorMap = new HashMap<Class,LazyComponentAccessor>();
    this.parent = null;
  }

  public ResourceTableImpl(ResourceTable resourceTable) {
    this.serviceComponentAccessorMap = new HashMap<ResourceKey,LazyComponentAccessor>();
    this.configurationComponentAccessorMap = new HashMap<Class,LazyComponentAccessor>();
    this.parent = resourceTable;
  }

  public void createIocContainer(MutableNameTable nameTable, MutableIocContainer resources) {
    //System.out.println(getClass().getName()+".createIocContainer");

    Application application = (Application)appComponentAccessor.getComponent();
    
    for (Iterator<Map.Entry<ResourceKey,LazyComponentAccessor>> iter = serviceComponentAccessorMap.entrySet().iterator();
          iter.hasNext();) {
      Map.Entry<ResourceKey,LazyComponentAccessor> entry = iter.next();
      ResourceKey key = entry.getKey();
      String uri = key.uri;

      LazyComponentAccessor componentAccessor = entry.getValue();
      //System.out.println(getClass().getName()+".createIocContainer registering " + uri);
      resources.registerServiceComponent(key.javaInterface,
        uri,componentAccessor.getComponent());
    }
    
    for (Iterator<Map.Entry<Class,LazyComponentAccessor>> iter = configurationComponentAccessorMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry<Class,LazyComponentAccessor> entry = iter.next();
      Class key = entry.getKey();
      LazyComponentAccessor componentAccessor = entry.getValue();
      resources.registerConfigurationComponent(key, componentAccessor.getComponent());
    }
  }

  public Object lookupServiceComponent(Class javaInterface, String uri) {
    //System.out.println(getClass().getName()+".lookupServiceComponent " + javaInterface.getName() + "#" + uri);
    
    ResourceKey key = new ResourceKey(javaInterface,uri);
    LazyComponentAccessor componentAccessor = serviceComponentAccessorMap.get(key);

    Object componentInstance = null;
    if (componentAccessor == null) {
      if (parent != null) {
        //System.out.println(getClass().getName()+".lookupServiceComponent Checking parent");
        componentInstance = parent.lookupServiceComponent(javaInterface,uri);
      }
    } else {
      componentInstance = componentAccessor.getComponent();
    }
    
    return componentInstance;
  }

  public void addServiceComponent(ConfigurationContext context,
  ServiceComponent component, String uri) {
    //System.out.println(getClass().getName()+".addServiceComponent " + uri);

    LazyComponentAccessor componentAccessor = new LazyComponentAccessor(component.getComponentAssembler(),
      context);
    
    Class[] javaInterfaces = component.getInterfaces();
    for (int i = 0; i < javaInterfaces.length; ++i) {
      Class javaInterface = javaInterfaces[i];
      ResourceKey key = new ResourceKey(javaInterface,uri);
      if (serviceComponentAccessorMap.containsKey(key)) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_DUPLICATE_NAME,
                                                                   context.getElement().getTagName(),
                                                                   uri,
                                                                   javaInterface.toString());
        throw new ServingXmlException(message);
      }
      serviceComponentAccessorMap.put(key,componentAccessor);
    }
  }

  public void setApplicationComponent(ConfigurationContext context, ServiceComponent component) {
    //System.out.println(getClass().getName()+".addServiceComponent " + uri);

    LazyComponentAccessor componentAccessor = new LazyComponentAccessor(component.getComponentAssembler(),
      context);

    this.appComponentAccessor = componentAccessor;
  }

  public void addConfigurationComponent(ConfigurationContext context,
  ConfigurationComponent component) {

    LazyComponentAccessor componentAccessor = new LazyComponentAccessor(component.getComponentAssembler(),
      context);

    configurationComponentAccessorMap.put(component.getInterface(),componentAccessor);
  }

  public Object lookupConfigurationComponent(Class javaInterface) {

    Object componentInstance = null;
    LazyComponentAccessor componentAccessor = configurationComponentAccessorMap.get(javaInterface);
    if (componentAccessor == null) {
      if (parent != null) {
        componentInstance = parent.lookupConfigurationComponent(javaInterface);
      }
    } else {
      componentInstance = componentAccessor.getComponent();
    }

    return componentInstance;
  }
  
  public void printDiagnostics(PrintStream printStream, NameTable nameTable) {
    if (serviceComponentAccessorMap.size() > 0) {
      String margin = "";
      printStream.println();
      printStream.println(margin + "Resources:");
      printStream.println();
      Iterator<Map.Entry<ResourceKey,LazyComponentAccessor>> iter = serviceComponentAccessorMap.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<ResourceKey,LazyComponentAccessor> entry = iter.next();
        ResourceKey key = entry.getKey();
        LazyComponentAccessor componentAccessor = entry.getValue();

        Object component = null;
        if (componentAccessor.isInitialized()) {
          try {
            component = componentAccessor.getComponent();
          } catch (Exception e) {
            //  Don't care
          }
        }
        String uri = key.uri;
        printStream.println(margin + uri + ")"); 
        printStream.println(margin + key.javaInterface.getName());
        if (component != null) {
          printStream.println(margin + "  - " + component.getClass().getName());
        }
      }
    }
  }
}


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

import java.util.Iterator;              
import java.util.HashMap;                     
import java.util.Map;
import java.io.PrintStream;

import com.servingxml.util.NameTable;

public class ComponentTableImpl implements ComponentTable {
  private ServiceComponent defaultServiceComponent;

  private Map<Integer,ConfigurationComponent> configurationComponentMap;
  private Map<Integer,AbstractComponent> abstractComponentMap;
  private Map<Integer,ServiceComponent> serviceComponentMap;
  private Map<Integer,Class> serviceInterfaceMap;
  private ComponentTable parent;

  public ComponentTableImpl() {
    this.configurationComponentMap = new HashMap<Integer,ConfigurationComponent>(); 
    this.abstractComponentMap = new HashMap<Integer,AbstractComponent>();      
    this.serviceComponentMap = new HashMap<Integer,ServiceComponent>();        
    this.serviceInterfaceMap = new HashMap<Integer,Class>();
    this.defaultServiceComponent = null;
  }

  public void addAbstractComponent(AbstractComponent component) {
    serviceInterfaceMap.put(component.getNameSymbol(),component.getInterface());
    abstractComponentMap.put(component.getNameSymbol(), component);
  }

  public void addServiceComponent(ServiceComponent component) {                         
    serviceInterfaceMap.put(component.getNameSymbol(),component.getComponentAssembler().getType());
    serviceComponentMap.put(component.getNameSymbol(), component);
  }

  public void addConfigurationComponent(ConfigurationComponent component) {
    //System.out.println(getClass().getName()+".addConfigurationComponent " + component.getNameSymbol());

    configurationComponentMap.put(component.getNameSymbol(), component);
  }

  public void initialize(NameTable nameTable) {
    initialize(nameTable, null, serviceInterfaceMap);
  }

  public void initialize(NameTable nameTable, ComponentTable parent, Map<Integer,Class> serviceInterfaceMap) {
    this.parent = parent;
    for (Iterator<Map.Entry<Integer,ServiceComponent>> iter = serviceComponentMap.entrySet().iterator(); 
          iter.hasNext();) {
      Map.Entry<Integer,ServiceComponent> entry = iter.next();
      Integer key = entry.getKey();
      ServiceComponent component = entry.getValue();
      component.initialize(nameTable, this, serviceInterfaceMap);
    }
    if (defaultServiceComponent != null) {
      defaultServiceComponent.initialize(nameTable, this, serviceInterfaceMap);
    }
  }
                                
  public void setDefaultServiceComponent(ServiceComponent component) {
    //System.out.println(getClass().getName()+".setDefaultServiceComponent " + component.getName()
      //+ " " + component.getComponentAssembler().getType().getName());

    if (component != null) {
      serviceInterfaceMap.put(component.getNameSymbol(),component.getComponentAssembler().getType());
      this.defaultServiceComponent = component;
    }
  }
                                                                  
  public ServiceComponent getDefaultServiceComponent() {
    //System.out.println(getClass().getName()+".getDefaultServiceComponent");

    ServiceComponent defaultComponent = defaultServiceComponent;
    //if (defaultComponent == null) {
      //System.out.println(getClass().getName()+".getDefaultServiceComponent default component is null");
    //}
    if (defaultComponent == null && parent != null) {
      defaultComponent = parent.getDefaultServiceComponent();
    }
    return defaultComponent;
  }

  public boolean isEmpty() {
    return defaultServiceComponent == null && abstractComponentMap.size() == 0
      && configurationComponentMap.size() == 0 && serviceComponentMap.size() == 0;
  }

  public ConfigurationComponent getConfigurationComponent(int nameSymbol) {
    //System.out.println(getClass().getName()+".getConfigurationComponent nameSymbol="+nameSymbol);
    for (Iterator<Map.Entry<Integer,ConfigurationComponent>> iter = configurationComponentMap.entrySet().iterator(); 
          iter.hasNext();) {
      Map.Entry<Integer,ConfigurationComponent> entry = iter.next();
      Integer key = entry.getKey();
      ConfigurationComponent component = entry.getValue();
      //System.out.println(key.toString() + " " + component.getComponentAssembler().getType().getName());
    }

    ConfigurationComponent component = configurationComponentMap.get(new Integer(nameSymbol));
    if (component == null && parent != null) {
      component = parent.getConfigurationComponent(nameSymbol);
    }
    //if (component == null) {
      //System.out.println("Component is null");
    //}
    return component;
  }

  public ServiceComponent getServiceComponent(int nameSymbol) {
    ServiceComponent component = findServiceComponent(nameSymbol);
    if (component == null) {
      //System.out.println(getClass()+".getServiceComponent Try default service" );
      component = getDefaultServiceComponent();
      //if (component != null) {
        //System.out.println(getClass()+".getServiceComponent Found default service " + component.getName());
      //}
    }
    return component;
  }

  public ServiceComponent findServiceComponent(int nameSymbol) {
    ServiceComponent component = serviceComponentMap.get(nameSymbol);
    if (component == null && parent != null) {
      component = parent.findServiceComponent(nameSymbol);
    }
    return component;
  }

  public AbstractComponent getAbstractComponent(int nameSymbol) {
    AbstractComponent component = abstractComponentMap.get(nameSymbol);
    if (component == null && parent != null) {
      component = parent.getAbstractComponent(nameSymbol);
    }
    return component;
  }

  public Class getInterface(int nameSymbol) {
    Class component = serviceInterfaceMap.get(nameSymbol);
    if (component == null && parent != null) {
      component = parent.getInterface(nameSymbol);
    }
    return component;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Default Service Component:\n");
    if (defaultServiceComponent != null) {
      sb.append(defaultServiceComponent.toString());
    }
    sb.append("\n");
    sb.append("Service Components:\n");
    for (Iterator<Map.Entry<Integer,ServiceComponent>> iter = serviceComponentMap.entrySet().iterator(); 
          iter.hasNext();) {
      Map.Entry<Integer,ServiceComponent> entry = iter.next();
      Integer key = entry.getKey();
      ServiceComponent component = entry.getValue();
      sb.append(" ");
      sb.append(component.toString());
      sb.append("\n");
    }

/*
    if (abstractComponentList.size() > 0) {
      sb.append("Abstract: ");
    }
    for (int i = 0; i < abstractComponentList.size(); ++i) {
      sb.append(abstractComponentList.get(i).toString() + "\n");
    }
    if (configurationComponentList.size() > 0) {
      sb.append("Configuration: ");
    }
    for (int i = 0; i < configurationComponentList.size(); ++i) {
      sb.append(configurationComponentList.get(i).toString() + "\n");
    }
    if (serviceComponentList.size() > 0) {
      sb.append("Service: ");
    }
    for (int i = 0; i < serviceComponentList.size(); ++i) {
      sb.append(serviceComponentList.get(i).toString() + "\n");
    }
*/    
    return sb.toString();
  }

  public void printDiagnostics(PrintStream printStream, NameTable nameTable) {
    for (Iterator<Map.Entry<Integer,ServiceComponent>> iter = serviceComponentMap.entrySet().iterator();
          iter.hasNext();) {
      Map.Entry<Integer,ServiceComponent> entry = iter.next();
      int key = entry.getKey();
      ServiceComponent component = entry.getValue();
      printStream.println("" + component.getName());
    }
    if (parent != null) {
      printStream.println("Parent:");
      parent.printDiagnostics(printStream,nameTable);
    }
  }
}

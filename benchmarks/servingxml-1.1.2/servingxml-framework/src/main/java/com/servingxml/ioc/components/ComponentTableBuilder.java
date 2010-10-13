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

import com.servingxml.util.xml.Receiver;
import com.servingxml.util.xml.AbstractReceiver;
import com.servingxml.util.xml.ReceiverContext;
import com.servingxml.util.Name;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.QualifiedName;

public class ComponentTableBuilder extends AbstractReceiver implements Receiver {
  private static final Name configurationComponentName = new QualifiedName(SystemConstants.SERVINGXML_IOC_NS_URI,
    "configurationComponent");
  private static final Name abstractComponentName = new QualifiedName(SystemConstants.SERVINGXML_IOC_NS_URI,
    "abstractComponent");
  private static final Name serviceComponentName = new QualifiedName(SystemConstants.SERVINGXML_IOC_NS_URI,
    "serviceComponent");
  private ConfigurationComponentBuilder configurationComponentBuilder;
  private AbstractComponentBuilder abstractComponentBuilder;
  private ServiceComponentBuilder serviceComponentBuilder;
  private ComponentTable componentTable;

  public ComponentTableBuilder(Name name, ComponentTable componentTable) {
    super(name, null);
    this.componentTable = componentTable;
  }                                  

  public ComponentTableBuilder(Name name, Receiver parent) {
    super(name, parent);
    this.componentTable = new ComponentTableImpl();
  }                                  

  public ComponentTableBuilder(Name name) {
    super(name, null);
    this.componentTable = new ComponentTableImpl();
  }                                  
                                        
  public void bind(ReceiverContext context) {

    abstractComponentBuilder = new AbstractComponentBuilder(abstractComponentName,this);
    configurationComponentBuilder = new ConfigurationComponentBuilder(configurationComponentName,this);
    serviceComponentBuilder = new ServiceComponentBuilder(serviceComponentName, this);
    Receiver[] receivers = new Receiver[]{configurationComponentBuilder, abstractComponentBuilder, 
      serviceComponentBuilder
      };
    super.bind(context, receivers);
  }

  protected void selfReceived(ReceiverContext context) {
    //System.out.println(getClass().getName()+".selfReceived");
  }

  public void childReceived(int symbol) {
    //System.out.println(getClass().getName()+".childReceived symbol="+symbol);
    if (symbol == configurationComponentBuilder.getSymbol()) {
      ConfigurationComponent component = configurationComponentBuilder.getConfigurationComponent();
      componentTable.addConfigurationComponent(component);
    } else if (symbol == abstractComponentBuilder.getSymbol()) {
      AbstractComponent component = abstractComponentBuilder.getAbstractComponent();
      componentTable.addAbstractComponent(component);
    } else if (symbol == serviceComponentBuilder.getSymbol()) {
      ServiceComponent component = serviceComponentBuilder.getServiceComponent();
      componentTable.addServiceComponent(component);
    }
  }

  public ComponentTable getComponentTable() {
    return componentTable;
  }
}

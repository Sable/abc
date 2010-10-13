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
import com.servingxml.util.QualifiedName;

public class ConfigurationComponentBuilder extends AbstractReceiver implements Receiver {
  private static final Name nameName = new QualifiedName("name");
  private static final Name assemblerClassName = new QualifiedName("assemblerClass");
  private static final Name[] attributeNames = new Name[]{nameName, assemblerClassName};

  private ConfigurationComponent configurationComponent;

  public ConfigurationComponentBuilder(Name elementName, Receiver parent) {
    super(elementName,attributeNames,parent);    
  }

  public void bind(ReceiverContext context) {
    Receiver[] receivers = new Receiver[0];
    super.bind(context, receivers);
  }

  public void startElement(ReceiverContext context) {
   //System.out.println(getClass().getName()+".startElement");
    super.startElement(context);
  }

  protected void selfReceived(ReceiverContext context) {
    String qname = getAttributeValue(0);
    Name name = context.createName(qname);
    int nameSymbol = context.getSymbol(name);
    String assemblerClassName = getAttributeValue(1);

    ComponentAssembler componentAssembler = ComponentAssemblerImpl.newInstance(assemblerClassName);
    configurationComponent = new ConfigurationComponent(name, nameSymbol, componentAssembler);
  }

  public ConfigurationComponent getConfigurationComponent() {
    return configurationComponent;
  }
}

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
import com.servingxml.util.NameEnumeration;        

public class ServiceComponentBuilder extends AbstractReceiver implements Receiver {

  private static final Name nameName = new QualifiedName("name");
  private static final Name assemblerClassName = new QualifiedName("assemblerClass");
  private static final Name baseName = new QualifiedName("base");
  private static final Name[] attributeNames = new Name[]{nameName, assemblerClassName, baseName};
  private static final Name serviceComponentName = new QualifiedName(SystemConstants.SERVINGXML_IOC_NS_URI,
    "serviceComponent");
  private ComponentTable childComponentTable;
                                                   
  private ServiceComponentBuilder childComponentBuilder;
  private ServiceComponent serviceComponent;

  public ServiceComponentBuilder(Name elementName, Receiver parent) {
    super(elementName, attributeNames, parent);
    childComponentTable = new ComponentTableImpl();
  }
                                             
  public void bind(ReceiverContext context) {
    //System.out.println(getClass().getName()+".bind");
    childComponentBuilder = new ServiceComponentBuilder(serviceComponentName, this);
    Receiver[] receivers = new Receiver[]{childComponentBuilder};
    //Receiver[] receivers = new Receiver[]{attributesBuilder};
    super.bind(context, receivers);
  }

  public void childReceived(int symbol) {
    //System.out.println(getClass().getName()+".childReceived");
    if (symbol == childComponentBuilder.getSymbol()) {
      ServiceComponent component = childComponentBuilder.getServiceComponent();
      if (component.getName().getLocalName().equals("*")) {
        String qname = getAttributeValue(0);
        //System.out.println(getClass().getName()+".childReceived " + qname);
        //if (serviceComponent != null) {
          //System.out.println(getClass().getName()+".childReceived setting default element="+serviceComponent.getName());
        //}
        childComponentTable.setDefaultServiceComponent(component);
      } else {
        childComponentTable.addServiceComponent(component);
      }
    }
  }

  protected void selfReceived(ReceiverContext context) {
    //System.out.println(getClass().getName()+".selfReceived");
    String qname = getAttributeValue(0);
    Name name = context.createName(qname);
    String assemblerClassName = getAttributeValue(1);
    String base = getAttributeValue(2);
    Name[] baseNames = new Name[0];
    if (base != null && base.length() > 0) {
      NameEnumeration elementEnum = NameEnumeration.parse(context, base);
      baseNames = elementEnum.getNames();
    } 
    int nameSymbol = context.getSymbol(name);
    int[] baseSymbols = new int[baseNames.length];
    for (int i = 0; i < baseNames.length; ++i) {
      baseSymbols[i] = context.getSymbol(baseNames[i]);
    }

    ComponentAssembler componentAssembler = ComponentAssemblerImpl.newInstance(assemblerClassName);
    Class returnType = componentAssembler.getType();

    ComponentInterface componentInterface;
    if (baseSymbols.length > 0) {
      componentInterface = new BaseComponentInterface(returnType, baseSymbols);
    } else {
      componentInterface = new ReturnTypeComponentInterface(returnType);
    }

    serviceComponent = new ServiceComponent(name, nameSymbol, componentAssembler,
      componentInterface, childComponentTable);
    childComponentTable = new ComponentTableImpl();
  }

  public ServiceComponent getServiceComponent() {
    return serviceComponent;
  }
}

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
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.QualifiedName;

public class AbstractComponentBuilder extends AbstractReceiver implements Receiver {
  private static final Name nameName = new QualifiedName("name");
  private static final Name javaInterfaceName = new QualifiedName("interface");
  private static final Name[] attributeNames = new Name[]{nameName, javaInterfaceName};
  private static final Name serviceComponentName = new QualifiedName(SystemConstants.SERVINGXML_IOC_NS_URI,
    "abstractComponent");
  private ComponentTable childComponentTable;
                                                   
  private ServiceComponentBuilder childComponentBuilder;
  private AbstractComponent abstractComponent;

  public AbstractComponentBuilder(Name elementName, Receiver parent) {
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
        //System.out.println(getClass().getName()+".childReceived setting default element="+getName());
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
    int nameSymbol = context.getSymbol(name);
    String javaInterfaceName = getAttributeValue(1);
    Class javaInterface;
    try {
      javaInterface = Thread.currentThread().getContextClassLoader().loadClass(javaInterfaceName);
    } catch (java.lang.ClassNotFoundException e) {
      String message = "1 Cannot find assembler class " + javaInterfaceName;
      throw new ServingXmlException(message);
    }
    abstractComponent = new AbstractComponent(name, nameSymbol, javaInterface, childComponentTable);
  }

  public AbstractComponent getAbstractComponent() {
    return abstractComponent;
  }
}

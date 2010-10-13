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

import java.util.Iterator;

// JAXP
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.ioc.components.ConfigurationComponent;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.ioc.components.ServiceComponent;
import com.servingxml.ioc.resources.MutableResourceTable;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.AbstractRuntimeContext;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.DomQnameContext;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


public abstract class AbstractConfigurationContext extends AbstractRuntimeContext
implements ConfigurationContext {

  private static final String INCLUDE = "include";
  private static final String DOCUMENTATION = "documentation";
  private final AbstractConfigurationContext tail;        
  private final MutableNameTable nameTable;       
  private final ResourceTable resourceTable;
  private final ComponentDictionary componentDictionary;
  private Element contextElement;
  private final QnameContext qnameContext;
  private final Record parameters;

  protected AbstractConfigurationContext(Record parameters, 
                                         MutableNameTable nameTable,
                                         ComponentDictionary componentDictionary, 
                                         String base, 
                                         ResourceTable resourceTable, 
                                         Element contextElement) {
    super(SystemConfiguration.getLogger());

    this.qnameContext = new DomQnameContext(nameTable, contextElement, base);

    this.tail = null;
    this.nameTable = nameTable;
    this.resourceTable = resourceTable;
    this.contextElement = contextElement;

    this.componentDictionary = componentDictionary;
    this.parameters = parameters;
  }

  protected AbstractConfigurationContext(AbstractConfigurationContext tail,
                                         Element contextElement, 
                                         ComponentDictionary componentDictionary,
                                         ResourceTable resourceTable) {
    super(SystemConfiguration.getLogger());

    this.qnameContext = new DomQnameContext(tail.getQnameContext(), contextElement);
    this.tail = tail;
    this.nameTable = tail.getNameTable();
    this.parameters = tail.getParameters();
    this.resourceTable = resourceTable;
    this.contextElement = contextElement;
    this.componentDictionary = componentDictionary;
  }

  protected AbstractConfigurationContext(AbstractConfigurationContext tail,
                                         Element contextElement, ResourceTable resourceTable) {
    super(SystemConfiguration.getLogger());

    this.qnameContext = new DomQnameContext(tail.getQnameContext(), contextElement);
    this.tail = tail;
    this.nameTable = tail.getNameTable();
    this.parameters = tail.getParameters();
    this.resourceTable = resourceTable;

    //  Revisit;
    this.contextElement = contextElement;
    this.componentDictionary = tail.getComponentDictionary();
  }

  public ConfigurationContext getParent() {
    return tail;
  }

  public Record getParameters() {
    return parameters;
  }

  public ResourceTable getResourceTable() {
    return resourceTable;
  }

  public Document getDocument() {
    //  tail will never be null
    return tail.getDocument();
  }

  public ConfigurationContext createInstance(Element contextElement, ComponentDictionary componentDictionary) {
    MutableResourceTable rt = new ResourceTableImpl(resourceTable);
    ConfigurationContextImpl context = new ConfigurationContextImpl(this,contextElement,componentDictionary,rt);
    initialize(context, componentDictionary, rt);
    return context;
  }

  private static void initialize(ConfigurationContext context, 
                                 ComponentDictionary componentDictionary,
                                 MutableResourceTable resourceTable) {

    Element contextElement = context.getElement();

    for (Iterator iter = DomHelper.createChildElementIterator(contextElement); iter.hasNext();) {
      Element componentElement = (Element)iter.next();
      String componentNamespaceUri = componentElement.getNamespaceURI();
      if (componentNamespaceUri == null) {
        componentNamespaceUri = "";
      } 
      String componentLocalName = componentElement.getLocalName();
      int instanceSymbol = context.getNameTable().getSymbol(componentNamespaceUri, componentLocalName);

      String ref = componentElement.getAttribute(SystemConstants.REF);
      if (ref.length() == 0) {
        String nqId = componentElement.getAttribute(SystemConstants.ID);
        if (nqId.length() == 0) {
          ConfigurationComponent configurationComponent = componentDictionary.getConfigurationComponent(
                                                                                                       instanceSymbol);
          if (configurationComponent != null) {
            ConfigurationContext childContext = context.createInstance(componentElement, componentDictionary);
            resourceTable.addConfigurationComponent(childContext, configurationComponent);
          }
        }
      } else {
        //System.out.println(AbstractConfigurationContext.class.getName()+".initialize ref="+ref);
      }
    }
  } 

  public ConfigurationContext createInstance(Element contextElement) {
    return createInstance(contextElement,componentDictionary);
  }

  public Name createName(String namespaceUri, String localName) {
    return nameTable.createName(namespaceUri, localName);
  }

  public Element getElement() {
    return contextElement;
  }

  public QnameContext getQnameContext() {
    return qnameContext;
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  public String getNamespaceUri(Element element) {
    String namespaceUri = DomHelper.getScopedAttribute(SystemConstants.NS,element);
    if (namespaceUri == null) {
      namespaceUri = qnameContext.getNamespaceUri("");
    }
    return namespaceUri;
  }

  public Object getConfigurationComponent(Class javaInterface) {

    return resourceTable.lookupConfigurationComponent(javaInterface);
  }

  protected Object getServiceComponent(Class javaInterface, Name componentName) {
    Object ref = resourceTable.lookupServiceComponent(javaInterface,componentName.toUri());

    return ref;
  }

  public Object getServiceComponent(Element element) {
    //System.out.println(getClass().getName()+".getServiceComponent " + element.getTagName());

    Object instance = null;

    String namespaceUri = element.getNamespaceURI();
    if (namespaceUri == null) {
      namespaceUri = "";
    }
    String localName = element.getLocalName();

    int instanceSymbol = nameTable.getSymbol(namespaceUri, localName);
    int includeId = getNameTable().getSymbol(SystemConstants.SERVINGXML_NS_URI,
                                             INCLUDE);
    int documentationId = getNameTable().getSymbol(SystemConstants.SERVINGXML_NS_URI,
                                             DOCUMENTATION);

    if (!(instanceSymbol == includeId || instanceSymbol == documentationId)) {
      String qRef = element.getAttribute(SystemConstants.REF);
      if (qRef.length() > 0) {
        //System.out.println(getClass().getName()+".getServiceComponent(Element,instanceSymbol,qRef) " + element.getTagName());
        instance = getServiceComponent(element,instanceSymbol,qRef);
        if (instance == null) {
          throw new ServingXmlException("Cannot resolve ref " + qRef);
        }
      } else {
        //System.out.println(getClass().getName()+".getServiceComponent(Element) " + element.getTagName());
        if (componentDictionary.getConfigurationComponent(instanceSymbol) == null) {
          ServiceComponent component = getServiceComponentOrDefault(instanceSymbol);
          if (component == null) {
            throw new ServingXmlException("Unknown component {" + namespaceUri + "}"+localName);
          }
          ConfigurationContext childContext = createInstance(element,component.getChildComponents());
          instance = component.getComponentAssembler().assemble(childContext);
        }
      }
    }

    //System.out.println("AbstractConfigurationContext.getServiceComponent " + new QualifiedName(namespaceUri,localName));

    return instance;
  }

  protected Object getServiceComponent(Element element, int instanceSymbol, String resourceQname) {
    //System.out.println(getClass()+".getServiceComponent");

    Object instance = null;


    String namespaceUri = element.getNamespaceURI();
    if (namespaceUri == null) {
      namespaceUri = "";
    }
    String localName = element.getLocalName();
    //System.out.println("AbstractConfigurationContext.getServiceComponent " + new QualifiedName(namespaceUri,localName));

    Class javaInterface = componentDictionary.getInterface(instanceSymbol);
    if (javaInterface == null) {
      Name name = nameTable.lookupName(instanceSymbol);
      String s = name == null ? "" : name.toQname(qnameContext);
      String message = "No java interface found for element " + s;
      throw new ServingXmlException(message);
    }
    if (resourceQname.length() > 0) {
      Name resourceName = DomHelper.createName(resourceQname, element);
      instance = getServiceComponent(javaInterface,resourceName);
    }


    return instance;
  }

  public ComponentDictionary getComponentDictionary() {
    return componentDictionary;
  }

  protected ServiceComponent getServiceComponentOrDefault(int instanceSymbol) {
    //if (contextElement != null) {
      //System.out.println(getClass()+".getServiceComponentOrDefault context="+contextElement.getTagName());
    //} else {
      //System.out.println(getClass()+".getServiceComponentOrDefault context is null");
    //}

    ServiceComponent component = componentDictionary.findServiceComponent(instanceSymbol);

    if (component == null) {
      //System.out.println(getClass().getName()+".getServiceComponentOrDefault Looking up default");
      component = componentDictionary.getDefaultServiceComponent();
    }
    //if (component == null) {
      //System.out.println(getClass()+".getServiceComponentOrDefault Default not found");
      //throw new ServingXmlException("Unknown component");
    //}
    //}

    return component;
  }

  public String getAppName() {
    return "system";
  }

  public String getUser() {
    return "";
  }
}


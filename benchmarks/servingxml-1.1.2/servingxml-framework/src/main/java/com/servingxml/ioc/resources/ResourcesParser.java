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
import java.net.URL;
import java.util.Iterator;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.servingxml.io.cache.SimpleCache;
import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.ioc.resources.AbstractConfigurationContext;
import com.servingxml.ioc.resources.TopLevelConfigurationContext;
import com.servingxml.ioc.components.ServiceComponent;
import com.servingxml.ioc.components.ConfigurationComponent;

/**
 * The <code>ResourcesParser</code> parses a resources document.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ResourcesParser {
  private static final String INCLUDE = "include";
  public static final String RESOURCES_LOCATION = "href";

  private final ComponentDictionary componentDictionary;
  private final MutableNameTable nameTable;

  public ResourcesParser(ComponentDictionary componentDictionary, 
                         MutableNameTable nameTable) {

    this.componentDictionary = componentDictionary;
    this.nameTable = nameTable;
  }

  public void initializeResources(ConfigurationContext context, MutableResourceTable resourceTable) {
    int includeId = nameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,
                                        INCLUDE);

    Element contextElement = context.getElement();
    //  Add includes
    Iterator includeIter = DomHelper.createChildElementIterator(contextElement);
    while (includeIter.hasNext()) {
      Element componentElement = (Element)includeIter.next();
      String componentNamespaceUri = componentElement.getNamespaceURI();
      if (componentNamespaceUri == null) {
        componentNamespaceUri = "";
      }
      String componentLocalName = componentElement.getLocalName();
      int instanceSymbol = nameTable.getSymbol(componentNamespaceUri,componentLocalName);
      if (instanceSymbol == includeId) {
        AbstractConfigurationContext topLevelContext = new IncludeConfigurationContext((AbstractConfigurationContext)context,componentElement,resourceTable);
        initInclude(topLevelContext, resourceTable);
      }
    }
    String rootNamespaceUri = contextElement.getNamespaceURI();
    if (rootNamespaceUri == null) {
      rootNamespaceUri = "";
    }
    String rootLocalName = contextElement.getLocalName();
    int rootInstanceSymbol = nameTable.getSymbol(rootNamespaceUri, rootLocalName);
    ServiceComponent rootComponent = componentDictionary.getServiceComponent(rootInstanceSymbol);
    if (rootComponent == null) {
      throw new ServingXmlException("No component associated with document element " + contextElement.getTagName());
    }
    resourceTable.setApplicationComponent(context, rootComponent);

    //  Add properties
    initialize(context,resourceTable);

  }

  public void initialize(ConfigurationContext context, MutableResourceTable resourceTable) {

    Element contextElement = context.getElement();

    for (Iterator iter = DomHelper.createChildElementIterator(contextElement); 
        iter.hasNext();) {
      Element componentElement = (Element)iter.next();
      String componentNamespaceUri = componentElement.getNamespaceURI();
      if (componentNamespaceUri == null) {
        componentNamespaceUri = "";
      }
      String componentLocalName = componentElement.getLocalName();
      int instanceSymbol = nameTable.getSymbol(componentNamespaceUri, componentLocalName);

      String ref = componentElement.getAttribute(SystemConstants.REF);
      if (ref.length() == 0) {
        String nqId = componentElement.getAttribute(SystemConstants.ID);
        if (nqId.length() == 0) {
          ConfigurationComponent configurationComponent = componentDictionary.getConfigurationComponent(
                                                                                                       instanceSymbol);
          if (configurationComponent != null) {
            ConfigurationContext childContext = context.createInstance(componentElement, componentDictionary);
            MutableResourceTable childResourceTable = new ResourceTableImpl(resourceTable);
            initialize(childContext, childResourceTable);
            resourceTable.addConfigurationComponent(childContext, configurationComponent);
          }
        }
      }
    }
    //  Add resources

    for (Iterator iter = DomHelper.createChildElementIterator(contextElement);
        iter.hasNext();) {
      Element componentElement = (Element)iter.next();

      String nqId = componentElement.getAttribute(SystemConstants.ID);
      if (nqId.length() > 0) {
        String componentNamespaceUri = componentElement.getNamespaceURI();
        if (componentNamespaceUri == null) {
          componentNamespaceUri = "";
        }
        String componentLocalName = componentElement.getLocalName();
        //System.out.println("componentLocalName="+componentLocalName);
        int instanceSymbol = nameTable.getSymbol(componentNamespaceUri, componentLocalName);
        //System.out.println(getClass().getName()+".initialize");
        ServiceComponent component = componentDictionary.getServiceComponent(instanceSymbol);
        //System.out.println(getClass().getName()+".initialize Looking for component " + componentElement.getTagName());
        if (component != null) {
          ConfigurationContext childContext = context.createInstance(componentElement,component.getChildComponents());
          MutableResourceTable childResourceTable = new ResourceTableImpl(resourceTable);
          initialize(childContext, childResourceTable);
          //System.out.println(getClass().getName()+".initialize Component found");
          //System.out.println(getClass().getName()+".initialize Adding service component " + instanceName.toUri());
          Name instanceName = childContext.getQnameContext().createName(nqId);
          resourceTable.addServiceComponent(childContext,component,instanceName.toUri());
        } //else {
        //componentDictionary.printDiagnostics(System.out,nameTable);
        //}
      }
    }

  } 

  protected void initInclude(AbstractConfigurationContext context, MutableResourceTable resourceTable) {
    //System.out.println(getClass().getName()+".initInclude enter" );

    Element includeNode = context.getElement();
    String location = includeNode.getAttribute(RESOURCES_LOCATION);
    if (location.length() > 0) {
      InputStream inStream = null;
      try {
        String systemId = context.getQnameContext().getBase();

        URL base = new URL(systemId);
        URL url = new URL(base,location);
        inStream = url.openStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Turn off validation, and turn on namespaces
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        //builder.setErrorHandler(this);

        InputSource source = new InputSource(inStream);      
        source.setSystemId(url.toString());
        Document document = builder.parse(source);

        Element root = document.getDocumentElement();
        String b = DomHelper.getScopedAttribute("xml:base",root);
        if (b == null) {
          root.setAttribute(SystemConstants.XML_BASE,url.toString());
        }
        //ConfigurationContext rootContext = context.createInstance(root);
        //MutableResourceTable resourceTable = new ResourceTableImpl(parentResourceTable);
        TopLevelConfigurationContext rootContext = new TopLevelConfigurationContext(context, document, resourceTable);

        initializeResources(rootContext, resourceTable);
      } catch (java.io.IOException e) {
        throw new ServingXmlException(e.getMessage(),e);
      } catch (ParserConfigurationException e) {
        throw new ServingXmlException(e.getMessage(),e);
      } catch (org.xml.sax.SAXException e) {
        throw new ServingXmlException(e.getMessage(),e);
      } finally {
        try {
          if (inStream != null) {
            inStream.close();
          }
        } catch (Exception t) {
        }
      }
    }
    //System.out.println(getClass().getName()+".initInclude leave" );
  }                        
}


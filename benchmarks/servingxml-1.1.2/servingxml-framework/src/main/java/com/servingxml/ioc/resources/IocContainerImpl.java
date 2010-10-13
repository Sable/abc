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

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.servingxml.io.cache.Cache;              
import com.servingxml.io.cache.SimpleCache;
import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.ioc.components.ConfigurationComponent;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.ioc.components.ServiceComponent;
import com.servingxml.ioc.resources.AbstractConfigurationContext;
import com.servingxml.ioc.resources.TopLevelConfigurationContext;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.util.xml.DomHelper;

/**
 * The <code>IocContainerImpl</code> instance holds a collection of resource 
 * objects indexed by name.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class IocContainerImpl implements MutableIocContainer, ResourceTable {
  private static final String RESOURCES = "resources";
  private static final String INCLUDE = "include";
  public static final String RESOURCES_LOCATION = "href";

  private final IocContainer parent;
  private final Map<ResourceKey,Object> resourceMap;         
  private final Map<Class,Object> configurationObjectMap;
  private final ComponentDictionary componentDictionary;
  private final MutableNameTable nameTable;
  private final SAXTransformerFactory transformerFactory;
  private QnameContext qnameContext = new SimpleQnameContext();

  public IocContainerImpl(MutableNameTable nameTable, ComponentDictionary componentDictionary,
                          SAXTransformerFactory transformerFactory) {

    this.resourceMap = new HashMap<ResourceKey,Object>();
    this.configurationObjectMap = new HashMap<Class,Object>();
    this.componentDictionary = componentDictionary;
    this.nameTable = nameTable;
    this.transformerFactory = transformerFactory;
    this.transformerFactory.setErrorListener(new DefaultTransformerErrorListener(SystemConfiguration.getSystemContext()));
    this.parent = null;
    registerConfigurationComponent(Cache.class,new SimpleCache());
  }

  public IocContainerImpl(ComponentDictionary componentDictionary,
                          IocContainer parent) {

    this.resourceMap = new HashMap<ResourceKey,Object>();
    this.configurationObjectMap = new HashMap<Class,Object>();
    this.componentDictionary = componentDictionary;
    this.nameTable = parent.getNameTable();
    this.transformerFactory = parent.getTransformerFactory();
    this.parent = parent;
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  public Object lookupServiceComponent(Class javaInterface, String uri) {
    //System.out.println(getClass().getName()+".lookupServiceComponent " + javaInterface.getName() + "#" + uri);
    ResourceKey key = new ResourceKey(javaInterface, uri);
    Object value = resourceMap.get(key);
    //if (value == null) {
    //System.out.println(getClass().getName()+".lookupServiceComponent Not found");
    //printDiagnostics(System.out);
    //}
    if (value == null && parent != null) {
      //System.out.println(getClass().getName()+".lookupServiceComponent Checking parent");
      value = parent.lookupServiceComponent(javaInterface,uri);
    }

    return value;
  }

  public Object lookupConfigurationComponent(Class javaInterface) {
    Object value = configurationObjectMap.get(javaInterface);
    if (value == null && parent != null) {
      value = parent.lookupConfigurationComponent(javaInterface);
    }

    return value;
  }

  public void registerServiceComponent(Class javaInterface, String uri, Object value) {
    //System.out.println(getClass().getName()+".registerServiceComponent " + javaInterface.getName()+"#"+uri);

    ResourceKey key = new ResourceKey(javaInterface, uri);
    resourceMap.put(key,value);
  }

  public void registerConfigurationComponent(Class javaInterface, Object value) {
    //System.out.println(getClass().getName()+".registerServiceComponent " + javaInterface.getName());

    configurationObjectMap.put(javaInterface,value);
  }

  public void printDiagnostics(PrintStream ps) {
    //  Print service map

    ps.println();
    ps.println("Resources");
    ps.println();
    Iterator<Map.Entry<ResourceKey,Object>> miter = resourceMap.entrySet().iterator();
    while (miter.hasNext()) {
      Map.Entry<ResourceKey,Object> entry = miter.next();
      ResourceKey resourceKey = entry.getKey();
      Object resource = entry.getValue();
      ps.println(resourceKey.toString() + " - " + resource.getClass().getName());
    }
    ps.println();

    //  Print content map
  } 

  /*
  * @deprecated since ServingXML 0.6.3: replaced by {@link IocContainerImpl#loadResources}
  */
  @Deprecated
  public void load(URL resourcesUrl) {
    loadResources(resourcesUrl, Record.EMPTY);
  }

  public void loadResources(URL resourcesUrl, Record parameters) {
    try {
      //System.out.println(getClass().getName()+".loadResources " + resourcesUrl);
      DocumentBuilderFactory resourcesBuilderFactory = DocumentBuilderFactory.newInstance();
      resourcesBuilderFactory.setValidating(false);
      resourcesBuilderFactory.setNamespaceAware(true);
      DocumentBuilder resourcesBuilder = resourcesBuilderFactory.newDocumentBuilder();

      String systemId = resourcesUrl.toString();
      Document resourcesDocument = resourcesBuilder.parse(systemId);

      loadResources(resourcesDocument, systemId, parameters);

    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void loadResources(Document resourcesDocument, String systemId, Record parameters) {
    try {
      //System.out.println(getClass().getName()+".loadResources " + resourcesUrl);

      MutableResourceTable resourceTable = new ResourceTableImpl(this);

      //System.out.println("*** Component Dictionary\n");
      //System.out.println(componentDictionary.toString());
      //System.out.println();
      //System.out.println("***\n");
      RootConfigurationContext context = new RootConfigurationContext(resourcesDocument,
                                                                      nameTable, 
                                                                      componentDictionary, 
                                                                      resourceTable, 
                                                                      systemId, 
                                                                      transformerFactory, 
                                                                      parameters);

      this.qnameContext = context.getQnameContext();

      initializeResources(context, resourceTable);

      //context.initialize();

      resourceTable.createIocContainer(nameTable,this);
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void initializeResources(ConfigurationContext context, MutableResourceTable resourceTable) {
    int includeId = getNameTable().getSymbol(SystemConstants.SERVINGXML_NS_URI,
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
      int instanceSymbol = getNameTable().getSymbol(componentNamespaceUri,componentLocalName);
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

    for (Iterator iter = DomHelper.createChildElementIterator(contextElement); iter.hasNext();) {
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

  public SAXTransformerFactory getTransformerFactory() {
    return transformerFactory;
  }

  public String[] getUris(Class javaInterface) {
    List<String> uriList = new ArrayList<String>();

    putUris(javaInterface, uriList);

    String[] uris = new String[uriList.size()];
    uris = uriList.toArray(uris);

    return uris;
  }

  public void putUris(Class javaInterface, List<String> uriList) {
    if (parent != null) {
      parent.putUris(javaInterface, uriList);
    }
    Iterator<Map.Entry<ResourceKey,Object>> iter = resourceMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<ResourceKey,Object> entry = iter.next();
      ResourceKey key = entry.getKey();
      if (key.javaInterface.equals(javaInterface)) {
        uriList.add(key.uri);
      }
    }
  }

  public QnameContext getQnameContext() {
    return qnameContext;
  }
}


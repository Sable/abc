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
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;
import javax.xml.transform.sax.SAXTransformerFactory;

// JAXP
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTable;
import com.servingxml.util.QnameContext;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.NameTableImpl;
import com.servingxml.io.cache.Cache;
import com.servingxml.io.cache.SimpleCache;

/**
 * The <code>SimpleIocContainer</code> instance holds a collection of resource 
 * objects indexed by name.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SimpleIocContainer implements IocContainer {
  
  private final IocContainer parent;
  private final Map<ResourceKey,Object> resourceMap;         
  private final Map<Class,Object> configurationObjectMap;
  private final MutableNameTable nameTable;
  private final SAXTransformerFactory transformerFactory;
  private final QnameContext qnameContext;

  public SimpleIocContainer(IocContainer parent) {

    this.resourceMap = new HashMap<ResourceKey,Object>();
    this.configurationObjectMap = new HashMap<Class,Object>();
    this.nameTable = parent.getNameTable();
    this.parent = parent;
    this.transformerFactory = parent.getTransformerFactory();
    this.qnameContext = new SimpleQnameContext();
    registerConfigurationComponent(Cache.class,new SimpleCache());
  }

  public SimpleIocContainer(NameTable nameTable, SAXTransformerFactory transformerFactory) {

    this.resourceMap = new HashMap<ResourceKey,Object>();
    this.configurationObjectMap = new HashMap<Class,Object>();
    this.nameTable = new NameTableImpl(nameTable);
    this.transformerFactory = transformerFactory;
    this.parent = null;
    this.qnameContext = new SimpleQnameContext();
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  public Object lookupServiceComponent(Class javaInterface, String uri) {
    //System.out.println(getClass().getName()+".lookupServiceComponent " + javaInterface.getName() + "#" + uri);
    ResourceKey key = new ResourceKey(javaInterface, uri);
    Object value = resourceMap.get(key);
    if (value == null && parent != null) {
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


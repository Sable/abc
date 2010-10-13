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

package com.servingxml.util.xml;

import org.w3c.dom.Element;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QnameContext;

public class DomQnameContext implements QnameContext {
  private final MutableNameTable nameTable;
  private final PrefixMap prefixMap;
  private final String base;
  
  public DomQnameContext(MutableNameTable nameTable, Element element) {
    this(nameTable, element, "");
  }
  
  public DomQnameContext(MutableNameTable nameTable, Element element,
  String base) {
    this.nameTable = nameTable;
    this.prefixMap = DomHelper.createPrefixMap(element);
    String namespaceUri = DomHelper.getScopedAttribute(SystemConstants.NS,element);
    String baseUri = DomHelper.getScopedAttribute(SystemConstants.XML_BASE,element);
    this.base = baseUri == null ? base : baseUri;
  }
  
  public DomQnameContext(QnameContext parent, Element element) {
    this.nameTable = parent.getNameTable();
    this.prefixMap = DomHelper.createPrefixMap(element, parent.getPrefixMap());
    String namespaceUri = DomHelper.getScopedAttribute(SystemConstants.NS,element);
    String baseUri = DomHelper.getScopedAttribute(SystemConstants.XML_BASE,element);
    this.base = baseUri == null ? parent.getBase() : baseUri;
  }
  
  public MutableNameTable getNameTable() {
    return nameTable;
  }
  
  public String getNamespaceUri(String prefix) {
    return prefixMap.getNamespaceUri(prefix);
  }

  public PrefixMap getPrefixMap() {
    return prefixMap;
  }

  public Name createName(String qname) {
    //System.out.println(getClass().getName()+".createName qname = " + qname);

    String localName = "";
    String prefix = "";
    String namespaceUri = "";

    if (qname != null) {
      int pos = qname.indexOf(":");
      if (pos < 0) {
        localName = qname;
        //namespaceUri = prefixMap.getNamespaceUri("");
      } else {
        prefix = qname.substring(0, pos);
        localName = qname.substring(pos + 1);
        namespaceUri = prefixMap.getNamespaceUri(prefix);
      }
    }

    Name name = nameTable.createName(namespaceUri, localName);

    return name;
  }

  public String getPrefix(String namespaceUri) {
    String prefix = prefixMap.getPrefix(namespaceUri);
    return prefix;
  }
  
  public Name createName(String namespaceUri, String localName) {
    Name name = nameTable.createName(namespaceUri, localName);
    return name;
  }
  
  public int lookupSymbol(Name name) {
    return nameTable.lookupSymbol(name);
  }
  
  public int lookupSymbol(String namespaceUri, String localName) {
    return nameTable.lookupSymbol(namespaceUri, localName);
  }
  
  public int getSymbol(Name name) {
    return nameTable.getSymbol(name);
  }
  
  public int getSymbol(String namespaceUri, String localName) {
    return nameTable.getSymbol(namespaceUri, localName);
  }
  
  public String getBase() {
    return base;
  }
}


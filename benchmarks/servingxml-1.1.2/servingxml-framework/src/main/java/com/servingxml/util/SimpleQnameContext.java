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

package com.servingxml.util;

import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

public class SimpleQnameContext implements QnameContext {
  
  private final MutableNameTable nameTable;
  private final PrefixMap prefixMap;

  public SimpleQnameContext() {
    this.nameTable = new NameTableImpl();
    PrefixMapImpl pm = new PrefixMapImpl();
    pm.setPrefixMapping(SystemConstants.SERVINGXML_NS_PREFIX,SystemConstants.SERVINGXML_NS_URI);
    this.prefixMap = pm;
  }

  public SimpleQnameContext(PrefixMap prefixMap) {
    this.nameTable = new NameTableImpl();
    this.prefixMap = prefixMap;
  }

  public SimpleQnameContext(MutableNameTable nameTable) {
    this.nameTable = nameTable;
    PrefixMapImpl pm = new PrefixMapImpl();
    pm.setPrefixMapping(SystemConstants.SERVINGXML_NS_PREFIX,SystemConstants.SERVINGXML_NS_URI);
    this.prefixMap = pm;
  }

  public SimpleQnameContext(MutableNameTable nameTable, PrefixMap prefixMap) {
    this.nameTable = nameTable;
    this.prefixMap = prefixMap;
  }

  public Name lookupName(int symbol) {
    return nameTable.lookupName(symbol);
  }

  public MutableNameTable getNameTable() {
    return nameTable;
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
  
  public Name createName(String qname) {
    String namespaceUri = "";
    String localName = "";
    String prefix = "";

    if (qname != null) {
      int pos = qname.indexOf(":");
      if (pos < 0) {
        localName = qname;
      } else {
        prefix = qname.substring(0, pos);
        localName = qname.substring(pos + 1);
        namespaceUri = prefixMap.getNamespaceUri(prefix);
      }
    }
    Name name = new QualifiedName(namespaceUri, localName);
    if (prefix.length() > 0) {
      nameTable.lookupCreateNamespaceIndex(namespaceUri);
    } else {
      nameTable.getSymbol(name);
    }
    return name;
  }

  public PrefixMap getPrefixMap() {
    return prefixMap;
  }

  public String getPrefix(String namespaceUri) {
    String prefix = prefixMap.getPrefix(namespaceUri);
    return prefix;
  }

  public String getBase() {
    return "";
  }

  public String getNamespaceUri(String prefix) {
    return prefixMap.getNamespaceUri(prefix);
  }
  
  public Name createName(String namespaceUri, String localName) {
    Name name = nameTable.createName(namespaceUri,localName);
    return name;
  }
}                               

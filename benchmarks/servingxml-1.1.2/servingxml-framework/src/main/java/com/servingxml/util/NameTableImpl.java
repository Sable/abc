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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintStream;

/**
 * Implements a nameSymbol table of <tt>Name</tt> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */ 

public class NameTableImpl implements MutableNameTable {
  private static final int INITIAL_NAME_SIZE = 100;
  private static final int INITIAL_NAMESPACE_SIZE = 50;
  private static final Name UNKNOWN = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"Unknown");

  private HashMap<Name,Integer> nameMap;
  private Name[] names;
  private int nameCount = 0;
  private String[] namespaces;
  private int namespaceCount = 0;

  public NameTableImpl() {
    namespaces = new String[INITIAL_NAMESPACE_SIZE];
    names = new Name[INITIAL_NAME_SIZE];
    nameMap = new HashMap<Name,Integer>(INITIAL_NAME_SIZE);
    namespaces[0] = "";
    namespaceCount = 1;
  }

  public NameTableImpl(NameTable nameTable) {
    namespaces = nameTable.getNamespaces();
    namespaceCount = namespaces.length;
    names = nameTable.getNames();
    this.nameCount = names.length;
    nameMap = new HashMap<Name,Integer>(INITIAL_NAME_SIZE);
    for (int i = 0; i < names.length; ++i) {
      Name name = names[i];
      nameMap.put(name,new Integer(i));
    }
  }

  public NameTableImpl(int initialNamespaceSize, int initialNameSize) {
    namespaces = new String[initialNamespaceSize];
    names = new Name[initialNameSize];
    nameMap = new HashMap<Name,Integer>(initialNameSize);
    namespaces[0] = "";
    namespaceCount = 1;
  }

  public int size() {
    return nameCount;
  }

  public String getNamespace(int namespaceIndex) {
    return namespaceIndex < namespaces.length ? namespaces[namespaceIndex] : "";
  }

  public Name[] getNames() {
    Name[] a = new Name[nameCount];
    System.arraycopy(names,0,a,0,nameCount);
    return a;
  }

  public String[] getNamespaces() {
    String[] a = new String[namespaceCount];
    System.arraycopy(namespaces,0,a,0,namespaceCount);
    return a;
  }

  public boolean containsSymbol(int nameSymbol) {
    return nameSymbol >= 0 && nameSymbol < nameCount;
  }

  public Name lookupName(int nameSymbol) {
    
    Name name;
    if (nameSymbol < 0 || nameSymbol >= nameCount) {
      //throw new IllegalArgumentException("Invalid nameSymbol " + nameSymbol);
      name = null;
    } else {
      name = names[nameSymbol];
    }

    return name;
  }

  public int getSymbol(String namespaceUri, String localName) {
    int namespaceIndex = lookupCreateNamespaceIndex(namespaceUri);
    Name name = new PooledName(this, namespaceIndex, localName);
    return getSymbol(name);
  }

  public int lookupCreateNamespaceIndex(String namespaceUri) {

    int namespaceIndex = -1;
    boolean found = false;
    for (int i=0; !found && i < namespaceCount; ++i) {
      String ns = namespaces[i];
      if (namespaceUri.equals(ns)) {
        namespaceIndex = i;
        found = true;
      }
    }
    if (!found) {
      if (namespaceCount == namespaces.length) {
        String[] oldUris = namespaces;
        int length = oldUris.length*2;
        if (length < INITIAL_NAMESPACE_SIZE) {
          length = INITIAL_NAMESPACE_SIZE;
        }
        namespaces = new String[length];
        if (namespaceCount > 0) {
          System.arraycopy(oldUris,0,namespaces,0,namespaceCount);
        }
      }
      namespaces[namespaceCount] = namespaceUri;
      namespaceIndex = namespaceCount;
      ++namespaceCount;
    }
    return namespaceIndex;
  }

  public int getSymbol(Name name) {
    Integer boxedSymbol = nameMap.get(name);
    int nameSymbol;
    if (boxedSymbol == null) {
      if (nameCount == names.length) {
        int len = names.length*2;
        if (len < INITIAL_NAME_SIZE) {
          len = INITIAL_NAME_SIZE;
        }
        Name[] oldNames = names;
        names = new Name[len];
        System.arraycopy(oldNames,0,names,0,nameCount);
      }
      names[nameCount] = name;
      nameMap.put(name,new Integer(nameCount));
      nameSymbol = nameCount;
      ++nameCount;
    } else {
      nameSymbol = boxedSymbol.intValue();
    }
    
    return nameSymbol;
  }

  public int lookupSymbol(String namespaceUri, String localName) {
    Name name = new QualifiedName(namespaceUri,localName);
    return lookupSymbol(name);
  }

  public int lookupSymbol(Name name) {
    Integer boxedIndex = nameMap.get(name);
    return boxedIndex == null ? -1 : boxedIndex.intValue();
  }

  public void printDiagnostics(PrintStream printStream) {
    printStream.println("namespaces");
    printStream.println("namespaceCount = " + namespaceCount + ", capacity = " + namespaces.length);
    for (int i = 0; i < namespaceCount; ++i) {
      String ns = namespaces[i];
      printStream.println("nameSymbol = " + i + ", namespaceUri = " + ns);
    }
    printStream.println("names");
    printStream.println("nameCount = " + nameCount + ", capacity = " + names.length);
    for (int i = 0; i < nameCount; ++i) {
      Name name = names[i];
      printStream.println("nameSymbol = " + i + ", name = " + name);
    }
    printStream.println("nameMap");
    printStream.println("size = " + nameMap.size());
    Iterator<Map.Entry<Name,Integer>> iter = nameMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Name,Integer> entry = iter.next();
      Name name = entry.getKey();
      Integer boxedSymbol = entry.getValue();
      printStream.println("nameSymbol = " + boxedSymbol + ", name = " + name);
    }
  }

  public Name createName(String namespaceUri, String localName) {

    int namespaceIndex = lookupCreateNamespaceIndex(namespaceUri);

    Name name = new PooledName(this, namespaceIndex, localName);
    getSymbol(name);
    return name;
  }
}

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

import java.io.PrintStream;

public class SynchronizedNameTable implements MutableNameTable {
  private final NameTable parentTable;
  private final MutableNameTable mutableTable;

  public SynchronizedNameTable(NameTable parentTable) {
    this.parentTable = parentTable;
    this.mutableTable = new NameTableImpl(parentTable);
  }

  public int size() {
    return parentTable.size() + mutableTable.size();
  }

  public int getSymbol(String namespaceUri, String localName) {
    int nameSymbol = parentTable.lookupSymbol(namespaceUri, localName);

    if (nameSymbol == -1) {
      synchronized(mutableTable) {
        nameSymbol = mutableTable.getSymbol(namespaceUri, localName);
      }
    }

    return nameSymbol;
  }

  public int getSymbol(Name name) {
    int nameSymbol = parentTable.lookupSymbol(name);
    if (nameSymbol == -1) {
      synchronized(mutableTable) {
        nameSymbol = mutableTable.getSymbol(name);
      }
    }
    return nameSymbol;
  }

  public int lookupCreateNamespaceIndex(String namespaceUri) {
    int namespaceIndex = -1;
    if (namespaceIndex == -1) {
      synchronized(mutableTable) {
        namespaceIndex = mutableTable.lookupCreateNamespaceIndex(namespaceUri);
      }
    }
    return namespaceIndex;
  }

  public String getNamespace(int namespaceIndex) {
    return parentTable.getNamespace(namespaceIndex);
  }

  public boolean containsSymbol(int nameSymbol) {
    return parentTable.containsSymbol(nameSymbol) ? true : mutableTable.containsSymbol(nameSymbol);
  }

  public Name lookupName(int nameSymbol) {
    Name name;
     
    if (parentTable.containsSymbol(nameSymbol)) {
      name = parentTable.lookupName(nameSymbol);
    } else {
      synchronized(mutableTable) {
        name = mutableTable.lookupName(nameSymbol);
      }
    }
    return name;
  }

  public int lookupSymbol(String namespaceUri, String localName) {

    int nameSymbol = parentTable.lookupSymbol(namespaceUri, localName);
    if (nameSymbol == -1) {
      synchronized(mutableTable) {
        nameSymbol = mutableTable.lookupSymbol(namespaceUri, localName);
      }
    }
    return nameSymbol;
  }

  public Name createName(String namespaceUri, String localName) {

    Name name;
    int symbol = parentTable.lookupSymbol(namespaceUri, localName);
    if (symbol == -1) {
      synchronized(mutableTable) {
        name = mutableTable.createName(namespaceUri, localName);
      }            
    } else {
      name = parentTable.lookupName(symbol);
    }
    return name;
  }

  public int lookupSymbol(Name name) {
    int nameSymbol = parentTable.lookupSymbol(name);
    if (nameSymbol == -1) {
      synchronized(mutableTable) {
        nameSymbol = mutableTable.lookupSymbol(name);
      }            
    }
    return nameSymbol;
  }
  /*
   * Returns a shallow copy of names
   */
  public Name[] getNames() {
    return mutableTable.getNames();
  }
  /*
   * Returns a shallow copy of namespaces
   */
  public String[] getNamespaces() {
    return mutableTable.getNamespaces();
  }
  /*
   *  Prints diagnostics for the name table content
   */
  public void printDiagnostics(PrintStream printStream) {
    parentTable.printDiagnostics(printStream);
    mutableTable.printDiagnostics(printStream);
  }
}


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

/**
 * Defines an immutable interface for a symbol table of <tt>Name</tt> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */ 

public interface NameTable {
  boolean containsSymbol(int nameSymbol);

  Name lookupName(int symbol);

  int lookupSymbol(String namespaceUri, String localName);

  int lookupSymbol(Name name);

  int size();

  /*
   *  Prints diagnostics for the name table content
   */                                       
  void printDiagnostics(PrintStream printStream);

  /*
   * Returns a shallow copy of names
   */
  Name[] getNames();

  /*
   * Returns a shallow copy of namespaces
   */
  String[] getNamespaces(); 

  String getNamespace(int namespaceIndex);
}

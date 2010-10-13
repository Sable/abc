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

/**
 * Defines an interface for a symbol table of <tt>Name</tt> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */ 

public interface MutableNameTable extends NameTable {
  int getSymbol(String namespaceUri, String localName);

  int getSymbol(Name name);

  int lookupCreateNamespaceIndex(String namespaceUri);

  Name createName(String namespaceUri, String localName);
}

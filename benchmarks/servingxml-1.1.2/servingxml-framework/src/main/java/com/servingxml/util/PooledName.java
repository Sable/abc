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
 *
 *  01/05/15
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class PooledName extends Name {

  private final NameTable nameTable;
  private final int namespaceIndex;
  private final String localName;
  private final int hashCode;

  public PooledName(NameTable nameTable, int namespaceIndex, String localName) {
    this.nameTable = nameTable;
    this.namespaceIndex = namespaceIndex;
    this.localName = localName;
    this.hashCode = localName.hashCode();
  }
  
  public boolean isEmpty() {
    return localName.length() == 0;
  }

  public String toUri() {
    return getNamespaceUri() + localName;
  }

  public final String getNamespaceUri() {
    String ns = nameTable.getNamespace(namespaceIndex);
    return ns;
  }

  public final String getLocalName() {
    return localName;
  }

  public String toQname(PrefixMap prefixMap) {
    String prefix = prefixMap.getPrefix(getNamespaceUri());
    return prefix.length() == 0 ? localName : prefix + ":" + localName;
  }

  public String toQname(QnameContext context) {
    String ns = nameTable.getNamespace(namespaceIndex);
    String prefix = context.getPrefix(ns);
    return prefix.length() == 0 ? localName : prefix + ":" + localName;
  }

  /**
   * Returns a hash code value for this name.
   *
   * @return a hash code value for this name.
   */

  public int hashCode() {
    return hashCode;
  }

  public boolean equals(Object o) {
    boolean isEqual = true;
    if (this != o) {
      Name other = (Name)o;
      isEqual = other.getLocalName().equals(localName) && other.getNamespaceUri().equals(getNamespaceUri());
    }
    return isEqual;
  }

  public String toString() {
    String s;

    if (namespaceIndex != 0) {
      StringBuilder buf = new StringBuilder();
      buf.append("{");
      buf.append(getNamespaceUri());
      buf.append("}");
      buf.append(localName);
      s = buf.toString();
    } else {
      s = localName;
    }
    return s;
  }

  public int compareTo(Object o) {
    int diff = 0;
    
    if (this != o) {
      Name b = (Name)o;

      diff = getNamespaceUri().compareTo(b.getNamespaceUri());
      if (diff == 0) {
        diff = localName.compareTo(b.getLocalName());
      }
    }
    return diff;
  }
}

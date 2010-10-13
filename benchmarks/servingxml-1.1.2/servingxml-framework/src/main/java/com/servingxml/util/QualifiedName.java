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

public class QualifiedName extends Name {                            

  private final String namespaceUri;
  private final String localName;
  private final int hashCode;

  /**
   * Creates a name with an empty namespace URI and an empty local name.
   */
  
  public QualifiedName() {
    this.namespaceUri = "";
    this.localName = "";
    this.hashCode = localName.hashCode();
  }

  /**
   * Creates a name with a local name, but no namespace URI.
   */

  public QualifiedName(String localName) {
    this.namespaceUri = "";
    this.localName = localName;
    this.hashCode = localName.hashCode();
  }

  /**
   * Creates a name with the namespace URI and local name.
   */

  public QualifiedName(String namespaceUri, String localName) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.hashCode = localName.hashCode();
  }

  public String toUri() {
    return namespaceUri + localName;
  }
  
  public boolean isEmpty() {
    return (namespaceUri.length() + localName.length()) == 0;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public String toQname(PrefixMap prefixMap) {
    String prefix = prefixMap.getPrefix(namespaceUri);
    return prefix.length() == 0 ? localName : prefix + ":" + localName;
  }

  public String toQname(QnameContext context) {
    String prefix = context.getPrefix(namespaceUri);
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
      isEqual = other.getLocalName().equals(localName) && other.getNamespaceUri().equals(namespaceUri);
    }
    return isEqual;
  }

  public String toString() {
    String s;

    if (namespaceUri.length() > 0) {
      StringBuilder buf = new StringBuilder();
      buf.append("{");
      buf.append(namespaceUri);
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

      diff = namespaceUri.compareTo(b.getNamespaceUri());
      if (diff == 0) {
        diff = localName.compareTo(b.getLocalName());
      }
    }
    return diff;
  }
}

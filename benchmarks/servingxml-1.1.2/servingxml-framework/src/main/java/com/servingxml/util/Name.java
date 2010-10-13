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

public abstract class Name implements Comparable {
  public static final Name EMPTY = new QualifiedName();

  abstract public boolean isEmpty();

  abstract public String getNamespaceUri();

  abstract public String getLocalName();

  abstract public String toUri();

  public abstract String toQname(QnameContext context);

  public abstract String toQname(PrefixMap prefixMap);

  /**
   * Parses a string to create a name
   * 
   * @param name A name, which may begin with a
   * namespace URI in curly braces ({}).
   */

  public static Name parse(String name) {

    String ns = "";
    String localName = "";

    int left = (name.length() > 0 && name.charAt(0) == '{') ? 0 : -1;
    if (left != -1) {
      int right = name.lastIndexOf('}');
      if (right != -1) {
        ns = name.substring(left+1,right);
        localName = name.substring(right+1);
      }
    } else {
      localName = name;
    }

    return new QualifiedName(ns, localName);
  }

  /**
   * Creates a name from the local name, where the namespace URI and prefix are empty strings.
   */

  public static Name createName(String localName) {
    return new QualifiedName(localName);
  }

  /**
   * Creates a name from the namespace URI and local name 
   */

  public static Name createName(String namespaceUri, String localName) {
    return new QualifiedName(namespaceUri, localName);
  }

  public static Name createName(String qname, PrefixMap prefixMap) {
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
    return name;
  }
}

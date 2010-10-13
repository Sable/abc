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

import java.util.HashSet;
import java.util.Set;

/**
 *                                       
 * @author  Daniel A. Parker   
 */

public class PrefixMapImpl implements PrefixMap {
  private final int INITIAL_CAPACITY = 10;
  private final PrefixMap tail;
  private PrefixMapping[] prefixMappings;
  private int count;

  public PrefixMapImpl() {
    this.count = 0;
    this.prefixMappings = new PrefixMapping[INITIAL_CAPACITY];
    this.tail = null;
  }

  public PrefixMapImpl(PrefixMap tail) {
    this.count = 0;
    this.prefixMappings = new PrefixMapping[INITIAL_CAPACITY];
    this.tail = tail;
  }

  public boolean containsPrefixMapping(String prefix, String namespaceUri) {
    boolean found = false;

    if (prefix.length() == 0 && namespaceUri.length() == 0) {
      found = true;
    } else {
      int index = prefixNamespaceIndex(prefix,namespaceUri);
      if (index != -1) {
        found = true;
      } else if (tail != null) {
        found = tail.containsPrefixMapping(prefix,namespaceUri);
      }
    }

    return found;
  }

  public void setPrefixMapping(String prefix, String namespaceUri) {
    int index = prefixIndex(prefix);
    if (index != -1) {
      prefixMappings[index] = new PrefixMappingImpl(prefix,namespaceUri);
    } else {
      prefixMappings = ensureCapacity(prefixMappings, count+1);
      prefixMappings[count] = new PrefixMappingImpl(prefix,namespaceUri);
      ++count;
    }
  }                                          

  public String getNamespaceUri(String prefix) {
    //System.out.println(getClass().getName() + ".getNamespaceUri enter");

    String namespaceUri = null;
    if (prefix.length() == 0) {
      namespaceUri = "";
    } else {
      int index = prefixIndex(prefix);
      if (index != -1) {
        namespaceUri = prefixMappings[index].getNamespaceUri();
      } else if (tail != null) {
        namespaceUri = tail.getNamespaceUri(prefix);
      }
    }

    if (namespaceUri == null) {
      throw new ServingXmlException("Unable to find namespace URI corresponding to prefix " + prefix);
    }

    return namespaceUri;
  }

  public String getPrefix(String namespaceUri) {
    //System.out.println(getClass().getName() + ".getNamespaceUri enter");

    String prefix = null;
    int index = namespaceIndex(namespaceUri);
    if (index != -1) {
      prefix = prefixMappings[index].getPrefix();
    } else if (tail != null) {
      prefix = tail.getPrefix(namespaceUri);
    } else if (namespaceUri.length() == 0) {
      prefix = "";
    }

    if (prefix == null) {
      throw new ServingXmlException("Unable to find prefix corresponding to namespace URI " + namespaceUri);
    }

    return prefix;
  }

  private int prefixIndex(String prefix) {
    int index = -1;
    for (int i = 0; index == -1 && i < count; ++i) {
      if (prefix.equals(prefixMappings[i].getPrefix())) {
        index = i;
      }
    }
    return index;
  }

  private int namespaceIndex(String namespaceUri) {
    int index = -1;
    for (int i = 0; index == -1 && i < count; ++i) {
      if (namespaceUri.equals(prefixMappings[i].getNamespaceUri())) {
        index = i;
      }
    }
    return index;
  }

  private int prefixNamespaceIndex(String prefix, String namespaceUri) {
    int index = -1;
    for (int i = 0; index == -1 && i < count; ++i) {
      if (prefix.equals(prefixMappings[i].getPrefix()) && namespaceUri.equals(prefixMappings[i].getNamespaceUri())) {
        index = i;
      }
    }
    return index;
  }

  private final PrefixMapping[] ensureCapacity(PrefixMapping[] arr, int required) {
    if (arr.length >= required)
      return arr;
    PrefixMapping[] bigger = new PrefixMapping[required + 16];
    System.arraycopy(arr, 0, bigger, 0, arr.length);
    return bigger;
  }

  public String getPrefixDeclarationString() {
    PrefixMapping[] prefixDeclarations = getPrefixDeclarations();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < prefixDeclarations.length; ++i) {
      PrefixMapping prefixMapping = prefixDeclarations[i];
      String prefix = prefixMapping.getPrefix();
      String namesapceUri = prefixMapping.getNamespaceUri();
      if (namesapceUri.length() > 0) {
        buf.append("xmlns");              
        if (prefix.length() > 0) {
          buf.append(":");
          buf.append(prefix);
        }
        buf.append("=\"");
        buf.append(namesapceUri);
        buf.append("\" ");
      }
    }
    String s = buf.toString();
    return s;
  }

  public PrefixMapping[] getLocalPrefixDeclarations() {
    PrefixMapping[] a = new PrefixMapping[count];
    if (count > 0) {
      System.arraycopy(prefixMappings, 0, a, 0, count);
    }

    return a;
  }

  public PrefixMap getParent() {
    return tail;
  }

  public PrefixMapping[] getPrefixDeclarations() {
    Set<PrefixMapping> prefixDeclarations = new HashSet<PrefixMapping>();
    getPrefixDeclarations(prefixDeclarations);
    PrefixMapping[] a = new PrefixMapping[prefixDeclarations.size()];
    a = prefixDeclarations.toArray(a);
    return a;
  }

  public void getPrefixDeclarations(Set<PrefixMapping> prefixDeclarations) {
    if (tail != null) {
      tail.getPrefixDeclarations(prefixDeclarations);
    }
    for (int i = 0; i < count; ++i) {
      prefixDeclarations.add(prefixMappings[i]);
    }
  }

  public static class PrefixMappingImpl implements PrefixMapping {
    private final String prefix;
    private final String namespaceUri;

    public PrefixMappingImpl(String prefix, String namespaceUri) {
      this.prefix = prefix;
      this.namespaceUri = namespaceUri;
    }

    public String getPrefix() {
      return prefix;
    }

    public String getNamespaceUri() {
      return namespaceUri;
    }

    public boolean equals(Object o) {
      boolean isEqual = true;
      if (o != this) {
        PrefixMapping rhs = (PrefixMapping)o;
        if (!(prefix.equals(rhs.getPrefix()) && namespaceUri.equals(rhs.getNamespaceUri()))) {
          isEqual = false;
        }
      }
      return isEqual;
    }

    public int hashCode() {
      return prefix.hashCode() + 31*namespaceUri.hashCode();
    }
  }

}


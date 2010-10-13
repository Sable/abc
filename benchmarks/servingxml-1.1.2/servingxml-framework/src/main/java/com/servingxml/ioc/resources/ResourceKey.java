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

package com.servingxml.ioc.resources;

import java.io.PrintStream;

// JAXP

/**
 * The <code>ResourceKey</code> object identifies a resource.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class ResourceKey {
  final Class javaInterface;
  final String uri;
  final int hash;

  ResourceKey(Class javaInterface, String uri) {
    this.javaInterface = javaInterface;
    this.uri = uri;
    this.hash = javaInterface.hashCode() + 31*uri.hashCode();
  }

  public boolean equals(Object obj) {
    boolean matches = true;
    if (obj != this) {
      ResourceKey key = (ResourceKey)obj;
      matches = key.javaInterface.equals(javaInterface) && key.uri.equals(uri);
    }

    return matches;
  }

  public int hashCode() {
    return hash;
  }

  public String toString() {
    return javaInterface.getName() + "-" + uri.toString();
  }
}

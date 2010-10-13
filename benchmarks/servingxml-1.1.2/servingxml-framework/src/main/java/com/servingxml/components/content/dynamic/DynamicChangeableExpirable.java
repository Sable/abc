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

package com.servingxml.components.content.dynamic;

import com.servingxml.io.cache.Expirable;

/**
 * Defines an interface for resource dynamic XML content.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class DynamicChangeableExpirable implements Expirable {

  private final DynamicChangeable dynamicExpirable;                
  private final Object parameters;

  public DynamicChangeableExpirable(DynamicChangeable dynamicExpirable, Object parameters) {

    this.dynamicExpirable = dynamicExpirable;
    this.parameters = parameters;
  }

  public long getLastModified(long timestamp) {
    long elapsed = System.currentTimeMillis() - timestamp;
    return dynamicExpirable.getLastModified(parameters,timestamp,elapsed);
  }

  public final boolean hasExpired(long timestamp) {
    long lastModified = getLastModified(timestamp);
    return lastModified > timestamp || lastModified < 0;
  }

  public boolean immediateExpiry() {
    return false;
  }
}

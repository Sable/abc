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


/**
 * Defines an interface for resource dynamic XML content.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface DynamicChangeable {
  static final DynamicChangeable ALWAYS_CHANGED = new AlwaysChanged();

   /** 
    * Called by the servingxml framework to determine whether the XML content has changed.
    * @param key the document key 
    * @param timestamp the time the node was created
    * @param elapsed 
    */
  long getLastModified(Object key, long timestamp, long elapsed)
  ;

  static final class AlwaysChanged implements DynamicChangeable {
    public long getLastModified(Object key, long timestamp, long elapsed) {
      return -1;
    }
  }
}

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

import java.util.Map;
import java.util.HashMap;

/**
 *
 *  01/05/15
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ChildDictionary<K,V> implements MutableDictionary<K,V> {
  private final Dictionary<K,V> parent;
  private final Map<K,V> backingMap;

  public ChildDictionary(Dictionary<K,V> parent) {
    this.parent = parent;
    this.backingMap = new HashMap<K,V>();
  }

  public V get(K key) {
    V value = backingMap.get(key);
    return value != null ? value : parent.get(key);
  }

  public void add(K key, V value) {
    backingMap.put(key,value);
  }

  public int size() {
    return backingMap.size() + parent.size();
  }

  public MutableDictionary<K,V> createChildDictionary() {
    return new ChildDictionary<K,V>(this);
  }
}

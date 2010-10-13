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

package com.servingxml.io.cache;

import java.util.HashMap;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SimpleCache implements Cache, CacheDictionary {
  
  private HashMap<Object,CacheEntry> cacheableMap = new HashMap<Object,CacheEntry>();
  private final Store store;

  public SimpleCache() {
    this.store = new SimpleStore();
  }
  
  public Object get(Key key) {

    Object data = store.get(this, key);
    return data;
  }

  public void add(Key key, Object data, Expirable expirable,
  RevalidationType revalidationType) {

    store.add(this,key,data,expirable,revalidationType);
  }

  public CacheEntry remove(Object key) {
    synchronized(cacheableMap) {
      return cacheableMap.remove(key);
    }
  }

  public CacheEntry get(Object key) {

    synchronized(cacheableMap) {
      CacheEntry entry = cacheableMap.get(key);
      
      return entry;
    }
  }

  public CacheEntry add(Object key, CacheEntry entry) {
    
    synchronized(cacheableMap) {
      return (CacheEntry)cacheableMap.put(key,entry);
    }
  }
}


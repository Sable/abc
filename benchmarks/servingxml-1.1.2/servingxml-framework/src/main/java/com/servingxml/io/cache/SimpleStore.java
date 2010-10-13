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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.servingxml.ioc.resources.ConfigurationListener;

/**
 * Manages a list of cache entries.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SimpleStore implements Store, ConfigurationListener {

  private final ReadWriteLock rwl = new ReentrantReadWriteLock();

  private CacheEntryList cacheList;

  public SimpleStore() { 
    cacheList = new CacheEntryList(rwl);
  }
  
  public void doStartUp() {
  }

  public void doShutDown() {
  }

  public int getCount() {
    return cacheList.getCount();
  }
  
  public void removeAll() {

    CacheEntry entry = cacheList.getHead();
    while (entry != null) {
      CacheEntry next = entry.getNext();
      remove(entry);
      entry = next;
    }
  }
                                   
  public Object get(CacheDictionary dictionary, Object key) {
    
    Object data = null;
    CacheEntry entry = dictionary.get(key);
    if (entry != null) {
      RevalidationType revalidationType = entry.getRevalidationType();
      if (!(revalidationType.revalidateSynch() && entry.hasExpired())) {
        data = entry.getData();
        entry.updateStatistics();
      }
    }
    return data;
  }
                                     
  protected void remove(CacheEntry entry) {

      entry.getCacheDictionary().remove(entry.getKey());
      cacheList.remove(entry);
  }

  public void add(CacheDictionary dictionary, Object key, 
  Object data, Expirable expirable, RevalidationType revalidationType) {
    
    CacheEntry entry = new CacheEntry(key,data,expirable,revalidationType,dictionary);
    entry.updateStatistics();
    CacheEntry old = dictionary.add(key,entry);
    if (old != null) {
      cacheList.remove(old);
    }
    cacheList.append(entry);
  }
}

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


/**
 * Implements an intrusive linked list entry for cache entries
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CacheEntry {
  
  private CacheEntry prev = null;
  private CacheEntry next = null;

  private final Object key;
  private final Object data;
  private final Expirable expirable;
  private final RevalidationType revalidationType;
  private final CacheDictionary dictionary;
  private final long timestamp;
  
  //  statistics
  private long lastAccessed;
  private int count;
  private Object statisticsLock = new Object();

  /**
   * Creates a <code>CacheEntry</code> object 
   * 
   */
  public CacheEntry(Object key, Object data, 
  Expirable expirable, RevalidationType revalidationType, CacheDictionary dictionary) {
    this.key = key;
    this.data = data;
    this.expirable = expirable;
    this.revalidationType = revalidationType;
    this.dictionary = dictionary;
    
    this.lastAccessed = System.currentTimeMillis();
    this.timestamp = expirable.getLastModified(lastAccessed);
    this.count = 0;
  }


  /**
   * Gets the next entry in the list
   */
  public CacheEntry getNext() {
    return next;
  }

  /**
   * Gets the previous entry in the list
   */
  public CacheEntry getPrev() {
    return prev;
  }
  
  /**
   * Sets the previous entry in the list
   */

  public void setPrev(CacheEntry entry) {
    prev = entry;
  }
  
  /**
   * Sets the next entry in the list
   */

  public void setNext(CacheEntry entry) {
    next = entry;
  }
  
  public Object getKey() {
    return key;
  }
  
  public Object getData() {
    return data;
  }
  
  public Expirable getExpirable() {
    return expirable;
  }
  
  public RevalidationType getRevalidationType() {
    return revalidationType;
  }
  
  public CacheDictionary getCacheDictionary() {
    return dictionary;
  }

  /**
   * Updates the last accessed time and hit count 
   */
  public final void updateStatistics() {
    synchronized(statisticsLock) {
      this.lastAccessed = System.currentTimeMillis();
      ++count;
    }
  }

  /**
   * Returns the hit count
   */
  public final int getCount() {
    synchronized(statisticsLock) {
      return count;
    }
  }

  /**
   * Returns the last accessed time (in milliseconds)
   */
  public final long getLastAccessed() {
    synchronized(statisticsLock) {
      return lastAccessed;
    }
  }

  /**
   * Invalidates the resource state
   */

  public boolean hasExpired() {
    return expirable.hasExpired(timestamp);
  }
}              


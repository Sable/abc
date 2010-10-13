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

package com.servingxml.components.cache;

import java.util.Properties;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.resources.ConfigurationListener;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.CacheEntryList;
import com.servingxml.io.cache.CacheEntry;
import com.servingxml.io.cache.RevalidationType;
import com.servingxml.io.cache.Store;
import com.servingxml.io.cache.CacheDictionary;

/**
 * Manages a list of cache entries.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MonitoredStore implements Store, ConfigurationListener {

  private boolean stopCheckMemory = false;
  private boolean stopCheckChanged = false;

  private int memoryCheckerPriority = Thread.NORM_PRIORITY;
  private int changedCheckerPriority = Thread.NORM_PRIORITY;

  private int SHORT_DELAY = 10;
  private final ReadWriteLock rwl = new ReentrantReadWriteLock();

  /**
   * Indicates how much memory should be left free in the JVM for
   * normal operation.
   */
  private int freeMemory;

  /**
   * Indicates how big the heap size can grow to before the cleanup thread kicks in.
   * The default value is based on the default maximum heap size of 60Mb.
   */
  private int heapSize;

  /**
   * Indicates the time in milliseconds to sleep between memory checks.
   */ 
  private long memoryCheckerInterval;

  /**
   * Indicates the time in milliseconds to sleep between memory checks.
   */ 
  private long changeCheckerInterval;

  private CacheEntryList cacheList;

  public MonitoredStore(Properties properties) {
    
    cacheList = new CacheEntryList(rwl);
    
    String freeMemoryValue = properties.getProperty(CacheConstants.FREE_MEMORY,"1000000");
    freeMemory = Integer.parseInt(freeMemoryValue.trim());
    String heapSizeValue = properties.getProperty(CacheConstants.HEAP_SIZE,"60000000");
    heapSize = Integer.parseInt(heapSizeValue);
    String memoryCheckerIntervalValue = properties.getProperty(CacheConstants.MEMORY_SIGNATURE_INTERVAL,"10");
    memoryCheckerInterval = Integer.parseInt(memoryCheckerIntervalValue);
    memoryCheckerInterval *= 1000;  // convert to milliseconds
    String memoryCheckerPriorityValue = properties.getProperty(CacheConstants.MEMORY_SIGNATURE_PRIORITY,"10");
    memoryCheckerPriority = Integer.parseInt(memoryCheckerPriorityValue.trim());
    String changeCheckerIntervalValue = properties.getProperty(CacheConstants.CHANGED_SIGNATURE_INTERVAL,"10");
    changeCheckerInterval = Integer.parseInt(changeCheckerIntervalValue.trim());
    changeCheckerInterval *= 1000;  // convert to milliseconds
    String changedCheckerPriorityValue = properties.getProperty(CacheConstants.MEMORY_SIGNATURE_PRIORITY,"10");
    changedCheckerPriority = Integer.parseInt(changedCheckerPriorityValue.trim());
  }
  
  public void doStartUp() {
    Thread checkMemoryChecker = new MemoryChecker();
    checkMemoryChecker.start();
    Thread checkChangedChecker = new ChangedChecker();
    checkChangedChecker.start();
  }
  public void doShutDown() {
    stopCheckMemory = true;
    stopCheckChanged = true;
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

  class MemoryChecker extends Thread {
    
    private Runtime jvm = Runtime.getRuntime();

    MemoryChecker() {
      setPriority(memoryCheckerPriority);
      setDaemon(true);
    }

    public final boolean isMemoryLow() {
      return jvm.totalMemory() > heapSize && jvm.freeMemory() < freeMemory;
    }

    public final void run() {

      while (!stopCheckMemory) {
        if (isMemoryLow()) {
          
          jvm.runFinalization();
          jvm.gc();

          CacheEntry entry;
          entry = cacheList.getHead();
          while (isMemoryLow() && entry != null) {
            CacheEntry next = entry.getNext();
            remove(entry);
            entry = next;
          }
        }
        try {
          Thread.currentThread().sleep(memoryCheckerInterval);
        } catch (InterruptedException ignore) {
        }
      }
    }
  }

  class ChangedChecker extends Thread {

    private Runtime jvm = Runtime.getRuntime();

    ChangedChecker() {
      setPriority(changedCheckerPriority);
      setDaemon(true);
    }

    public final void run() {

      while (!stopCheckChanged) {
        CacheEntry entry;
        entry = cacheList.getHead();
        while (entry != null) {
          CacheEntry next = entry.getNext();
          RevalidationType revalidationType = entry.getRevalidationType();
          if (revalidationType.revalidateAsynch()) {
            boolean changed = false;
            try {
              changed = entry.hasExpired();
            } catch (ServingXmlException e) {
              changed = true;
            }
            if (changed) {
              remove(entry);
            }
          }
          entry = next;
          //  At this point we give up the lock on the cache list to
          //  give other threads a chance.
          try {
            sleep(SHORT_DELAY);
          } catch (InterruptedException ignore) {
          }
        }
        
        try {
          Thread.currentThread().sleep(changeCheckerInterval);
        } catch (InterruptedException ignore) {
        }
      }
    }
  }
}

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

import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;


import com.servingxml.io.cache.RevalidationType;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.CacheEntry;
import com.servingxml.io.cache.CacheDictionary;
import com.servingxml.io.cache.Store;
import com.servingxml.io.cache.RevalidationType;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CacheTest extends TestCase {
  
  private CacheIndexImpl index = new CacheIndexImpl();
  private Store store = null;

  public CacheTest(String name) {
    super(name);
  }
  
  protected void setUp() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("free-memory", "1000000");
    properties.setProperty("heap-size", "60000000");
    properties.setProperty("memory-checker-interval", "10");
    properties.setProperty("memory-checker-priority", "5");
    properties.setProperty("changed-checker-interval", "10");
    properties.setProperty("changed-checker-priority", "5");
    store = new MonitoredStore(properties);
  }

  public void testEmpty() {
  }

  public void xtestAsynchCache() throws Exception {
    final String sourceMethod = "testAsynchCache";

    store.removeAll();
    assertTrue("count = 0", store.getCount() == 0);
    
    for (int i = 0; i < 10000; ++i) {
      Integer key = new Integer(i);
      store.add(index,key,key,new SlidingExpirable(1000),
        RevalidationType.ASYNCH_REVALIDATION);
      Object data = store.get(index,key);
      assertTrue("data != null", data != null);
      assertTrue("date == " + key,data.equals(key));
    }
    for (int i = 0; i < 10000; ++i) {
      Integer key = new Integer(i);
      Object data = store.get(index,key);
      assertTrue("data != null", data != null);
      assertTrue("date == " + key,data.equals(key));
    }
  }

  public void xtestSynchCache() throws Exception {
    final String sourceMethod = "testSynchCache";
    for (int i = 0; i < 10000; ++i) {
      Integer key = new Integer(i);
      store.add(index,key,key,new SlidingExpirable(1000),
        RevalidationType.SYNCH_REVALIDATION);
      Object data = store.get(index,key);
      assertTrue("data != null", data != null);
      assertTrue("date == " + key,data.equals(key));
    }
    for (int i = 0; i < 10000; ++i) {
      Integer key = new Integer(i);
      Object data = store.get(index,key);
      assertTrue("data != null", data != null);
      assertTrue("date == " + key,data.equals(key));
    }
    try {
      Thread.currentThread().sleep(1000);
    } catch (InterruptedException ignore) {
    }
    for (int i = 0; i < 10000; ++i) {
      Integer key = new Integer(i);
      Object data = store.get(index,key);
      assertTrue("data == null", data == null);
    }
  
  }

  public void xtestSynchCache2() throws Exception {
    final String sourceMethod = "testAsynchCache2";
    store.removeAll();
    assertTrue("count = 0", store.getCount() == 0);
    assertTrue("size = 0", index.size() == 0);
    
    CacheIndexImpl storeIndex = new CacheIndexImpl();
    for (int i = 0; i < 100; ++i) {
      if (i % 2 == 1) {
        Thread storeTester = new SynchCacheTester(storeIndex,Expirable.NEVER_EXPIRES, Thread.MAX_PRIORITY);
        storeTester.start();
      } else {
        Thread storeTester = new SynchCacheTester(storeIndex,Expirable.NEVER_EXPIRES, Thread.MIN_PRIORITY);
        storeTester.start();
      }
    }
    
    try {
      Thread.currentThread().sleep(10000);
    } catch (InterruptedException ignore) {
    }
    assertTrue("size = 0", index.size() == 0);
    assertTrue("size = " + storeIndex.size(), storeIndex.size() == 10000);
    assertTrue("count = " + store.getCount(), store.getCount() == 10000);
  }

  class SynchCacheTester extends Thread {
  
    private final Expirable expirable;
    private final CacheDictionary storeIndex;
  
    SynchCacheTester(CacheDictionary storeIndex, Expirable expirable, int priority) {
      setPriority(priority);
      setDaemon(true);
      this.expirable = expirable;
      this.storeIndex = storeIndex;
    }
  
    public final void run() {
      try {
        final String sourceMethod = "run";
        for (int i = 0; i < 10000; ++i) {
          Integer key = new Integer(i);
          store.add(storeIndex,key,key,expirable,
            RevalidationType.SYNCH_REVALIDATION);
          Object data = store.get(storeIndex,key);
          assertTrue("data != null", data != null);
          assertTrue("date == " + key,data.equals(key));
        }
        for (int i = 0; i < 10000; ++i) {
          Integer key = new Integer(i);
          Object data = store.get(storeIndex,key);
          assertTrue("data != null " + i, data != null);
          assertTrue("date == " + key,data.equals(key));
        }
        try {
          Thread.currentThread().sleep(1000);
        } catch (InterruptedException ignore) {
        }
        for (int i = 0; i < 10000; ++i) {
          Integer key = new Integer(i);
          Object data = store.get(storeIndex,key);
          assertTrue("data != null " + i, data != null);
        }
      }
      catch (Exception e) {
      }
    }
  }
}

class CacheIndexImpl implements CacheDictionary {
  private HashMap index = new HashMap();
  
  public int size() {
    return index.size();
  }
  
  public synchronized CacheEntry get(Object key) {
    return (CacheEntry) index.get(key);
  }
  public synchronized CacheEntry add(Object key, CacheEntry entry) {
    return (CacheEntry)index.put(key,entry);
  }
  public synchronized CacheEntry remove(Object key) {
    return (CacheEntry)index.remove(key);
  }
}



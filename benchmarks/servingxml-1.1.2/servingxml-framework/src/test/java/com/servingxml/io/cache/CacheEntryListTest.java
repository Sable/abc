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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CacheEntryListTest extends TestCase {
  private ReadWriteLock rwl = new ReentrantReadWriteLock();

  public CacheEntryListTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testResourceStateList() {
/*    
    CacheEntryList cacheList = new CacheEntryList(rwl);
    Link link1 = new Link();
    cacheList.append(link1);
    assertTrue("link1 == head", link1 == cacheList.getHead());
    assertTrue("link1 == tail", link1 == cacheList.getTail());
    Link link2 = new Link();
    cacheList.append(link2);
    assertTrue("link1 == head", link1 == cacheList.getHead());
    assertTrue("link2 == head", !(link2 == cacheList.getHead()));
    assertTrue("link2 == tail", link2 == cacheList.getTail());
    Link link3 = new Link();
    cacheList.append(link3);
    assertTrue("link1 == head", link1 == cacheList.getHead());
    assertTrue("link2 == head", !(link2 == cacheList.getHead()));
    assertTrue("link2 == tail", !(link2 == cacheList.getTail()));
    assertTrue("link3 == head", !(link3 == cacheList.getHead()));
    assertTrue("link3 == tail", link3 == cacheList.getTail());

    cacheList.remove(link2);
    assertTrue("link1 == head", link1 == cacheList.getHead());
    assertTrue("link3 == head", !(link3 == cacheList.getHead()));
    assertTrue("link3 == tail", link3 == cacheList.getTail());

    cacheList.remove(link1);
    assertTrue("link3 == head", link3 == cacheList.getHead());
    assertTrue("link3 == tail", link3 == cacheList.getTail());
    
    cacheList.remove(link3);
    assertTrue("head == null", cacheList.getHead() == null);
    assertTrue("tail == null", cacheList.getTail() == null);
*/    
  }

}                    


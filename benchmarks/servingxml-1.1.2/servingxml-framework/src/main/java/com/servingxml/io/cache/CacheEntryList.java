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

import com.servingxml.util.Asserter;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * A doubly linked list of cache entries. The head is the least recently used entry and 
 * the tail is the most recently used entry. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CacheEntryList {
  private static final String sourceClass = CacheEntryList.class.getName();

  private CacheEntry head = null;
  private CacheEntry tail = null;
  private final Lock readLock;  //  sync for read-only methods
  private final Lock writeLock;  //  sync for mutative methods
  private int count = 0;

  public CacheEntryList(ReadWriteLock rwl) {
    readLock = rwl.readLock();
    writeLock = rwl.writeLock();
  }

  public int getCount() {
    boolean wasInterrupted = beforeRead();
    try {
      return count;
    } finally {
      afterRead(wasInterrupted);
    }
  }

  public CacheEntry getHead() {
    boolean wasInterrupted = beforeRead();
    try {
      return head;
    } finally {
      afterRead(wasInterrupted);
    }
  }

  public CacheEntry getTail() {
    boolean wasInterrupted = beforeRead();
    try {
      return tail;
    } finally {
      afterRead(wasInterrupted);
    }
  }

  public void remove(CacheEntry link) {

    writeLock.lock();
    try {
      if (link.getPrev() != null) {
        link.getPrev().setNext(link.getNext());
      } else if (head == link) {
        head = link.getNext();
      }
      if (link.getNext() != null) {
        link.getNext().setPrev(link.getPrev());
      } else if (tail == link) {
        tail = link.getPrev();
      }
      link.setPrev(null);
      link.setNext(null);
      --count;
    } finally {
      writeLock.unlock();
    }
  }

  public void append(CacheEntry entry) {
    final String sourceMethod = "append";

    Asserter.assertTrue(sourceClass,sourceMethod,"entry",entry != null);

    writeLock.lock();
    try {
      entry.setPrev(tail);
      entry.setNext(null);
      if (tail != null) {
        tail.setNext(entry);
        tail = entry;
      } else {
        head = entry;
        tail = entry;
      }
      ++count;
      Asserter.assertTrue(sourceClass,sourceMethod,"head",head != null);
      Asserter.assertTrue(sourceClass,sourceMethod,"tail",tail != null);
    } finally {
      writeLock.unlock();
    }

  }

  /** Try to acquire sync before a reader operation; record failure **/
  private boolean beforeRead() {
    try {
      readLock.lock();
      return false;
    } finally {
    }
  }

  /** Clean up after a reader operation **/
  private void afterRead(boolean wasInterrupted) {
    if (wasInterrupted) {
      Thread.currentThread().interrupt();
    } else {
      readLock.unlock();
    }
  }

}

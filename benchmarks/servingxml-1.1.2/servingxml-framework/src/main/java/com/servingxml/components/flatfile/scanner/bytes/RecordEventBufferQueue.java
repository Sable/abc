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

package com.servingxml.components.flatfile.scanner.bytes;

public class RecordEventBufferQueue {
  private RecordEventBuffer head;
  private RecordEventBuffer tail;
  private int count = 0;

  public final void enqueue(RecordEventBuffer buf) {
    if (tail != null) {
      tail.next = buf;
      buf.previous = tail;
      tail = buf;
    } else {
      head = tail = buf;
      tail.next = tail.previous = null;
    }
    ++count;
  }

  public final RecordEventBuffer dequeue() {
    RecordEventBuffer buf = head;
    if (buf != null) {
      head = buf.next;
      if (head != null) {
        head.previous = null;
      } else {
        head = tail = null;
      }
      buf.previous = buf.next = null;
      --count;
    }
    return buf;
  }

  public final RecordEventBuffer pop() {
    RecordEventBuffer buf = tail;
    if (tail != null) {
      tail = buf.previous;
      if (tail != null) {
        tail.next = null;
      } else {
        head = tail = null;
      }
      buf.previous = buf.next = null;
      --count;
    }
    return buf;
  }

  public RecordEventBuffer head() {
    return head;
  }

  public RecordEventBuffer tail() {
    return tail;
  }

  public int size() {
    return count;
  }

  public boolean isEmpty() {
    return count == 0;
  }
}


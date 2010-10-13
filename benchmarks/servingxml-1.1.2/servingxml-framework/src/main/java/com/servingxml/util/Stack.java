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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;                       

public class Stack<T> {
  private final List<T> list = new ArrayList<T>();

  public final void push(T o) {
    list.add(o);
  }

  public final T pop() {
    int index = list.size() - 1;
    return list.remove(index);
  }

  public final void enqueue(T o) {
    list.add(0,o);
  }

  public final T dequeue() {
    return list.remove(0);
  }

  public final T peek() {
    int index = list.size() - 1;
    return list.get(index);
  }

  public T peek(int i) {
    int index = list.size() - 1 - i;
    return list.get(i);
  }

  public final boolean empty() {
    return list.size() == 0;
  }

  public Iterator iterator() {
    return list.iterator();
  }

  public int size() {
    return list.size();
  }
}


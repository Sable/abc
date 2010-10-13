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

package com.servingxml.components.flatfile.scanner.characters;

import java.io.IOException;

public abstract class AbstractRecordBlock implements RecordBlock {

  protected char[] buffer;
  protected int start;
  protected int capacity;
  protected int maxCapacity;
  protected int length;
  protected int reserved;

  public AbstractRecordBlock(char[] buffer, int start, int capacity, int maxCapacity) {
    this.buffer = buffer;
    this.start = start;
    this.capacity = capacity;

    this.maxCapacity = maxCapacity;
    this.length = 0;
    this.reserved = 0;
  }

  public int capacity() {
    return this.capacity;
  }

  public int length() {
    return this.length;
  }

  public void clear() {
    remove(length);
  }

  public void remove(int charCount) {
   //System.out.println(getClass().getName()+".remove Enter: length="+length+", size="+size()+",maxCapacity="+maxCapacity + new String(buffer,start,capacity));
    if (length > 0) {
      int n = length >= charCount ? charCount : length;
      int newLength = length - n;
      int newCapacity = capacity - n;
      System.arraycopy(buffer, start+n, buffer, start, newCapacity);
      this.capacity = newCapacity;
      this.length = newLength;
      if (this.maxCapacity < Integer.MAX_VALUE) {
        maxCapacity = capacity;
      }
    }
   //System.out.println(getClass().getName()+".remove Leave: length="+length+", size="+size()+",maxCapacity="+maxCapacity + new String(buffer,start,capacity));
  }
  public int getReserved() {
    return reserved;
  }

  public void setReserved(int reserved) {
    this.reserved = reserved;
  }

  public int maxCapacity() {
    return maxCapacity;
  }

  public void next(int n) throws IOException {
   //System.out.println("Before next "+ n + ": length="+length + ", size=" + size() + ", capacity="+ capacity + ", maxCapacity="+maxCapacity);
    if (length <= size() - n) {
      length += n;
    } else if (capacity < maxCapacity) { // n > size() - length
      int m = n - (size()-length);
      read(m);
      if (length <= size()-n) {
        length += n;
      } else { // n > size() - length
        //int diff = size() - length;
        //if (diff > 0) {
        //  length += diff;
        //}
        length = size();
      }
    } else {
      length = size();
    }
    if (length == size() && capacity < maxCapacity) {
      read(1);
    }
   //System.out.println("After next "+ n + ": length="+length + ", size=" + size() + ", maxCapacity="+maxCapacity);
  }

  protected abstract void read(int n) throws IOException;

  protected void grow(int n) {
    //System.out.println("grow before: n="+n + ", buffer.length="+buffer.length);
    if (capacity + n >= buffer.length) {
      int newLength = (capacity+n)*2;
      char[] temp = new char[newLength];
      System.arraycopy(buffer,0,temp,0,capacity);
      buffer = temp;  
      start = 0;
    }
    //System.out.println("grow after: n="+n + ", buffer.length="+buffer.length);
  }

  public char[] buffer() {
    return buffer;
  }

  public int start() {
    return start;
  }                   

  public int size() {
    return capacity - reserved;
  }
}

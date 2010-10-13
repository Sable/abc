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

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public class CharArrayBuilder {
  private char[] data;
  private int size;

  public CharArrayBuilder() {
    this.data = new char[128];
    this.size = 0;
  }

  public CharArrayBuilder(int capacity) {
    this.data = new char[capacity];
    this.size = 0;
  }

  public char[] buffer() {
    return data;
  }

  public int length() {
    return this.size;
  }

  public int start() {
    return 0;
  }

  public void clear() {
    size = 0;
  }

  public final void grow(int n) {

    if (size + n >= data.length) {
      int capacity = data.length;
      do {
        capacity *= 2;
      } while (capacity <= size + n);
      char[] newBuffer = new char[capacity];
      System.arraycopy(data,0,newBuffer,0,size);
      data = newBuffer;  
    }
  }

  public void append(char b) {
    grow(1);
    data[size++] = b;
  }

  public void put(int start, char[] charArray) {
    int delta = start - size + charArray.length;
    if (delta > 0) {
      grow(delta);
    }
    System.arraycopy(charArray, 0, data, start, charArray.length);
    if (start+charArray.length > size) {
      size = start+charArray.length;
    }
  }

  public void put(int start, char[] charArray, char padChar) {
    int delta = start - size + charArray.length;
    if (delta > 0) {
      grow(delta);
    }
    if (start > size) {
      Arrays.fill(data,size,start,padChar);
    }
    System.arraycopy(charArray, 0, data, start, charArray.length);
    if (start+charArray.length > size) {
      size = start+charArray.length;
    }
  }

  public void append(char[] charArray) {
    grow(charArray.length);
    System.arraycopy(charArray, 0, data, size, charArray.length);
    size += charArray.length;
  }

  public void append(char[] charArray, int start, int len) {
    grow(len);
    System.arraycopy(charArray, start, data, size, len);
    size += len;
  }

  public int append(Reader is, int count) 
  throws IOException {
    grow(count);
    int len = is.read(data, size, count);
    if (len >= 0) {
      size += len;
    }
    return len;
  }

  public void prepend(char[] charArray, int start, int len) {
    grow(len);
    System.arraycopy(data, 0, data, len, size);
    System.arraycopy(charArray, start, data, 0, len);
    size += len;
  }

  public char[] toCharArray() {
    char[] charArray = new char[size];
    System.arraycopy(data,0,charArray,0,size);
    return charArray;
  }
}

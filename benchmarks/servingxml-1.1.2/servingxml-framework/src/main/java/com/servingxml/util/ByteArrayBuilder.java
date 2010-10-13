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
import java.io.InputStream;

public class ByteArrayBuilder {
  private byte[] data;
  private int size;

  public ByteArrayBuilder() {
    this.data = new byte[128];
    this.size = 0;
  }

  public ByteArrayBuilder(int capacity) {
    this.data = new byte[capacity];
    this.size = 0;
  }

  public byte[] buffer() {
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
      byte[] newBuffer = new byte[capacity];
      System.arraycopy(data,0,newBuffer,0,size);
      data = newBuffer;  
    }
  }

  public void append(byte b) {
    grow(1);
    data[size++] = b;
  }

  public void put(int start, byte[] bytes) {
    int delta = start - size + bytes.length;
    if (delta > 0) {
      grow(delta);
    }
    System.arraycopy(bytes, 0, data, start, bytes.length);
    if (start+bytes.length > size) {
      size = start+bytes.length;
    }
  }

  public void append(byte[] bytes) {
    grow(bytes.length);
    System.arraycopy(bytes, 0, data, size, bytes.length);
    size += bytes.length;
  }

  public void append(byte[] bytes, int start, int len) {
    grow(len);
    System.arraycopy(bytes, start, data, size, len);
    size += len;
  }

  public int append(InputStream is, int count) 
  throws IOException {
    grow(count);
    int len = is.read(data, size, count);
    if (len >= 0) {
      size += len;
    }
    return len;
  }

  public void prepend(byte[] bytes, int start, int len) {
    grow(len);
    System.arraycopy(data, 0, data, len, size);
    System.arraycopy(bytes, start, data, 0, len);
    size += len;
  }

  public byte[] toByteArray() {
    byte[] bytes = new byte[size];
    System.arraycopy(data,0,bytes,0,size);
    return bytes;
  }
}

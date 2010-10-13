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

package com.servingxml.components.flatfile;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.servingxml.util.CharsetHelper;

public class ByteRecordOutput implements RecordOutput {

  private final Charset charset;
  private byte[] data;
  private int size;
  private int position;
  private final byte padByte;

  public ByteRecordOutput(Charset charset, byte padByte) {
    this.data = new byte[128];
    this.size = 0;
    this.position = 0;
    this.charset = charset;
    this.padByte = padByte;
  }

  public byte[] toByteArray() {
    byte[] result = new byte[size];
    System.arraycopy(data,0,result,0,size);
    return result;
  }

  public char[] toCharArray() {
    return CharsetHelper.bytesToCharacters(data,0,size, charset);
  }

  public byte[] buffer() {
    return data;
  }

  public int start() {
    return 0;
  }

  public int length() {
    return size;
  }

  public void writeBytes(byte[] value) {
    append(value);
  }

  public void writeByte(byte value) {
    append(value);
  }

  public void writeCharacters(char[] value) {
    byte[] bytes = CharsetHelper.charactersToBytes(value, charset);
    append(bytes);
  }

  public void writeString(String s) {
    byte[] bytes = CharsetHelper.stringToBytes(s, charset);
    append(bytes);
  }

  public void setPosition(int position) {
    if (position >= 0) {
      this.position = position;
      if (position > size) {
        int delta = position - size + 1;
        grow(delta);
        Arrays.fill(data, size, position, padByte);
        size = position;
      }
    }
  }

  public int getPosition() {
    return position;
  }

  public int getSize() {
    return position;
  }

  public void clear() {
    size = 0;
    position = 0;
  }

  public void pushBack(char[] sequence) {
    byte[] bytes = CharsetHelper.charactersToBytes(sequence, charset);

    if (bytes.length > 0) {
      grow(bytes.length);
      System.arraycopy(bytes, 0, data, size, bytes.length);
      size += bytes.length;
    }
  }

  private void append(byte[] bytes) {

    int delta = position - size + bytes.length;
    if (delta > 0) {
      grow(delta);
    }
    System.arraycopy(bytes, 0, data, position, bytes.length);
    position += bytes.length;

    if (position > size) {
      size = position;
    }
  }

  private void append(byte value) {

    int delta = position - size + 1;
    if (delta > 0) {
      grow(delta);
    }
    data[position++] = value;

    if (position > size) {
      size = position;
    }
  }

  private final void grow(int n) {

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
}


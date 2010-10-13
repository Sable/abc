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

public class CharRecordOutput implements RecordOutput {

  private final Charset charset;
  private char[] data;
  private int size;
  private int position;
  private final char padByte;

  public CharRecordOutput(Charset charset, char padByte) {
    this.data = new char[128];
    this.size = 0;
    this.position = 0;
    this.charset = charset;
    this.padByte = padByte;
  }

  public byte[] toByteArray() {
    return CharsetHelper.charactersToBytes(data,0,size, charset);
  }

  public char[] toCharArray() {
    char[] result = new char[size];
    System.arraycopy(data,0,result,0,size);
    return result;
  }

  public void writeBytes(byte[] value) {
    char[] result = CharsetHelper.bytesToCharacters(value,charset);
    append(result);
  }

  public void writeByte(byte value) {
    char[] result = CharsetHelper.bytesToCharacters(new byte[]{value},charset);
    append(result);
  }

  public void writeCharacters(char[] value) {
    append(value);
  }

  public void pushBack(char[] sequence) {

    if (sequence.length > 0) {
      grow(sequence.length);
      System.arraycopy(sequence, 0, data, size, sequence.length);
      size += sequence.length;
    }
  }

  public void writeString(String s) {
    append(s.toCharArray());
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
    return size;
  }

  public void clear() {
    size = 0;
    position = 0;
  }

  private void append(char[] sequence) {

    if (sequence.length > 0) {
      int delta = position - size + sequence.length;
      if (delta > 0) {
        grow(delta);
      }
      System.arraycopy(sequence, 0, data, position, sequence.length);
      position += sequence.length;
  
      if (position > size) {
        size = position;
      }
    }
  }

  private void append(char value) {

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
      char[] newBuffer = new char[capacity];
      System.arraycopy(data,0,newBuffer,0,size);
      data = newBuffer;  
    }
  }
}


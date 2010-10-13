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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.DelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordReceiver;

public class FixedLengthByteRecordInput implements RecordInput {
  private final RecordBlock block;
  private final Charset charset;
  private int currentIndex;
  private int last;

  public FixedLengthByteRecordInput(RecordBlock block, Charset charset) {
    this.block = block;
    this.currentIndex = 0;
    this.last = 0;
    this.charset = charset == null ? Charset.defaultCharset() : charset;
  }

  public byte[] toByteArray() {
    byte[] newData = new byte[block.length()];
    System.arraycopy(block.buffer(),block.start(),newData,0,block.length());
    return newData;
  }

  public char[] toCharArray() {
    return CharsetHelper.bytesToCharacters(block.buffer(),block.start(),block.length(), charset);
  }

  public boolean done() {
    return currentIndex >= block.size() && block.capacity() >= block.maxCapacity();   
  }

  public int readBytes(byte[] value) throws IOException {
    int len = reserve(value.length);
    //System.out.println(getClass().getName()+".readBytes index=" + currentIndex + ", width=" + value.length + ", resevred="+len);
    if (len > 0) {
      System.arraycopy(block.buffer(), block.start()+currentIndex, value, 0, len);
      currentIndex += len;
    }
    updateLast();
    return len;
  }

  public String readString(int width) throws IOException {
    int len = reserve(width);
    //System.out.println(getClass().getName()+".readBytes index=" + currentIndex + ", width=" + width + ", resevred="+len);
    String s;
    if (len > 0) {
      s = CharsetHelper.bytesToString(block.buffer(), block.start()+currentIndex, len, charset);
      currentIndex += len;
      //System.out.println(getClass().getName()+".readString " + currentIndex);
    } else {
      s = "";
    }
    updateLast();          
    return s;
  }

  public String readString(int maxLength, FlatFileOptions flatFileOptions)
  throws IOException {
    return null;
  }

  public String[] readStringArray(int maxLength, FlatFileOptions flatFileOptions) 
  throws IOException {
    return null;
  }

  public int getPosition() {
    return currentIndex;
  }

  public int getLast() {
    return last;
  }

  public void setPosition(int index) throws IOException {
    if (index < block.length()) {
      currentIndex = index;
    } else {
      int n = index - block.length() + 1;
      block.next(n);
      if (index < block.length()) {
        currentIndex = index;
      } else {
        currentIndex = block.length();
      }
    }
    updateLast();
    //System.out.println(getClass().getName()+".setPosition index=" + index + ", pos=" + currentIndex);
  }

  public int reserve(int len) throws IOException {
    //System.out.println(getClass().getName()+".reserve Enter: len = " + len + ", current=" + currentIndex + ", length="+length() + " " + new String(buffer(),start(),length()));
    if (currentIndex+len > block.length()) {
      int n = currentIndex+len - block.length();
      //System.out.println(getClass().getName()+".reserve grow = " + n);
      block.next(n);
      if (currentIndex+len > block.length()) {
        len = block.length() - currentIndex;
      }
      //System.out.println(getClass().getName()+".reserve Leave: len = " + len + ", current=" + currentIndex + ", length="+length() + " " + new String(buffer(),start(),length()));
    }
    return len;
  }

  public void wipe() {
    if (last > 0) {
      //System.out.println(getClass().getName()+".wipe last=" + last);
      block.remove(last);
      currentIndex = 0;
      last = 0;
    }
  }

  public void updateLast() {
    if (last < currentIndex) {
      last = currentIndex;
    }
  }

  public Charset getCharset() {
    return charset;
  }

  public void readRepeatingGroup2(ServiceContext context, 
                                  Flow flow, 
                                  int count, 
                                  FlatFileOptions flatFileOptions,
                                  DelimiterExtractor[] recordDelimiters, 
                                  int recordDelimiterStart, 
                                  int recordDelimiterCount, 
                                  int maxRecordWidth,
                                  FlatRecordReader flatRecordReader,
                                  RecordReceiver recordReceiver) {
  }

  public RecordInput readSegment(FlatFileOptions flatFileOptions) {
    return this;
  }

  public RecordInput readSegment(int segmentLength) {
    try {
      byte[] data = new byte[segmentLength];
      int length = readBytes(data);
      RecordInput segmentInput = new ByteRecordInput(data, 0, length, getCharset());
      //String s = new String(data,6,length-6,getCharset().name());
      //System.out.println("segment="+s);
      return segmentInput;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public RecordInput concatenate(RecordInput ri) {
    RecordInput recordInput = this;
    byte[] rhBytes = ri.toByteArray();
    if (rhBytes.length > 0) {
      int capacity = (block.length() + rhBytes.length)*2;
      byte[] newBuffer = new byte[capacity];
      System.arraycopy(block.buffer(),block.start(),newBuffer,0,block.length());
      System.arraycopy(rhBytes,0,newBuffer,block.length(),rhBytes.length);
      recordInput = new ByteRecordInput(newBuffer, 0, block.length()+rhBytes.length, 
                                                 getCharset());
    }
    return recordInput;
  }

  public RecordInput concatenate(RecordInput ri, int beginIndex) {
    //System.out.println(getClass().getName()+".concatenate length="+length()+", beginIndex = "+beginIndex+", rhs len="+ri.length());

    RecordInput recordInput = this;
    byte[] rhBytes = ri.toByteArray();
    if (rhBytes.length - beginIndex > 0) {
      int capacity = (block.length() + rhBytes.length-beginIndex)*2;
      byte[] newBuffer = new byte[capacity];
      System.arraycopy(block.buffer(),block.start(),newBuffer,0,block.length());
      System.arraycopy(rhBytes,beginIndex,newBuffer,block.length(),rhBytes.length-beginIndex);
      recordInput = new ByteRecordInput(newBuffer, 0, block.length()+rhBytes.length-beginIndex, 
                                                 getCharset());
      //System.out.println(getClass().getName()+".concatenate recordInput.length() = "+ recordInput.length());
    }
    return recordInput;
  }

  public String toString() {
    String s = CharsetHelper.bytesToString(block.buffer(), block.start(), block.length(), charset);
    return s;
  }

  public int length() {
    return block.length();
  }

  public int start() {
    return block.start();
  }
}


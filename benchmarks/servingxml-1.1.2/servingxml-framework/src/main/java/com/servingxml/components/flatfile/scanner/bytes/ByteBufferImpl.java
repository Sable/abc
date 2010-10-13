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

import java.io.InputStream;
import java.io.IOException;

import com.servingxml.components.flatfile.options.ByteBuffer;
import com.servingxml.util.ServingXmlException;

public class ByteBufferImpl implements ByteBuffer {
  protected static final int BLOCK_LENGTH = 512;

  protected byte[] buffer;
  protected int start;
  protected int length;
  protected int maxLength;
  protected int position;
  protected int reserved;
  private final InputStream is;

  public ByteBufferImpl(InputStream is) {
    this.buffer = new byte[BLOCK_LENGTH];
    this.start = 0;
    this.length = 0;
    this.maxLength = Integer.MAX_VALUE;
    position = -1;
    reserved = 0;
    this.is = is;
  }

  // Preconditions:
  //    !done()
  protected void read(int n) throws IOException {
    if (position >= length - n) {
      int maxBytes = n > BLOCK_LENGTH ? n : BLOCK_LENGTH;
      grow(maxBytes);
      int bytesRead = is.read(buffer,start+length,maxBytes);
      if (bytesRead >= 0) {
        length += bytesRead;
        if (bytesRead < maxBytes) {
          maxLength = length;
        }
      } else {
        maxLength = length;
      }
    }
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getReserved() {
    return reserved;
  }

  public void setReserved(int reserved) {
    this.reserved = reserved;
  }

  public int maxLength() {
    return maxLength;
  }

  public void next() throws IOException {
    if (!done()) {
      if (position+1 < length-reserved) {
        ++position;
      } else {
        read(1+reserved);
        if (!done()) {
          ++position;
        }
      }
    }
  }

  public void next(int n) throws IOException {
    if (!done()) {
      if (position + n < length - reserved) {
        position += n;
      } else {
        read(n + reserved);
        int diff = position + n - (length - reserved);
        position += diff <= 0 ? n : n - diff;
      }
    }
  }

  public boolean done() {
    return position >= maxLength-reserved;
  }

  protected boolean eof() {
    return position >= maxLength;
  }

  public byte current() {
    if (start+position < 0) {
      throw new ServingXmlException("position less than zero in record buffer");
    }
    return buffer[start+position];
  }

  protected void grow(int n) {
    if (length + n >= buffer.length) {
      int size = (length+n)*2;
      byte[] temp = new byte[size];
      System.arraycopy(buffer,0,temp,0,length);
      buffer = temp;  
      start = 0;
    }
  }

  public byte[] buffer() {
    return buffer;
  }

  public int start() {
    return start;
  }

  public int length() {
    return length;
  }

  public boolean startsWith(byte[] prefix) throws IOException {

    boolean found = false;

    if (prefix.length > 0) {
      if (length - position < prefix.length) {
        read(prefix.length);
      }

      if (length - position >= prefix.length) {
        found = true;
        for (int i = 0; found && i < prefix.length; ++i) {
          if (prefix[i] != buffer[start+position+i]) {
            found = false;
          }
        }
      }
    }
    return found;
  }

  public void clear() {
    if (position >= 0) {
      int len = length - position;
      if (len > 0) {
        System.arraycopy(buffer, start+position, buffer, start, len);
        length = len;
      } else {
        length = 0;                     
      }
      maxLength -= position;
    }
    position = -1;
  }
}

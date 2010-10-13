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

import java.io.Reader;
import java.io.IOException;
import java.nio.charset.Charset;

import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.util.ServingXmlException;

public class CharBufferImpl implements CharBuffer {
  protected static final int BLOCK_LENGTH = 512;
  protected char[] buffer;
  protected int start;
  protected int length;
  protected int maxLength;
  protected int position;
  protected int reserved;
  private final Reader reader;

  public CharBufferImpl(Reader reader) {
    this.buffer = new char[BLOCK_LENGTH];
    this.start = 0;
    this.length = 0;
    this.maxLength = Integer.MAX_VALUE;
    position = -1;
    reserved = 0;
    this.reader = reader;
  }

  // Preconditions:
  //    !done()
  protected void read(int n) throws IOException {
    if (position >= length - n) {
      int maxChars = n > BLOCK_LENGTH ? n : BLOCK_LENGTH;
      grow(maxChars);
      int charsRead = reader.read(buffer,start+length,maxChars);
      if (charsRead >= 0) {
        length += charsRead;
        if (charsRead < maxChars) {
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

  public char current() {
    if (start+position < 0) {
      throw new ServingXmlException("position less than zero in record buffer");
    }
    return buffer[start+position];
  }

  protected void grow(int n) {
    if (length + n >= buffer.length) {
      int size = (length+n)*2;
      char[] temp = new char[size];
      System.arraycopy(buffer,0,temp,0,length);
      buffer = temp;  
      start = 0;
    }
  }

  public char[] buffer() {
    return buffer;
  }

  public int start() {
    return start;
  }

  public int length() {
    return length;
  }

  public boolean startsWith(char[] prefix) throws IOException {

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

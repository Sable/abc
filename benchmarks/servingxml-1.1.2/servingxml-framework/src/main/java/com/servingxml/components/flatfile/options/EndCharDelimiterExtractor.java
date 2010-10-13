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

package com.servingxml.components.flatfile.options;

import java.io.IOException;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.CharArrayHelper;
import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.components.flatfile.parsing.TokenReceiver;

public final class EndCharDelimiterExtractor implements CharDelimiterExtractor {
  private final char[] value;
  private final char[] continuationSequence;
  private final char[] escapeSequence;

  public EndCharDelimiterExtractor(char[] value) {
    this.value = value;
    this.continuationSequence = SystemConstants.EMPTY_CHAR_ARRAY;
    this.escapeSequence = SystemConstants.EMPTY_CHAR_ARRAY;
  }

  public EndCharDelimiterExtractor(char[] value, char[] continuationSequence) {
    this.value = value;
    this.continuationSequence = continuationSequence;
    this.escapeSequence = SystemConstants.EMPTY_CHAR_ARRAY;
  }

  public EndCharDelimiterExtractor(char[] value, char[] escapedBy, char[] continuationSequence) {
    this.value = value;
    this.continuationSequence = continuationSequence;
    this.escapeSequence = escapedBy.length == 0 ? SystemConstants.EMPTY_CHAR_ARRAY 
      : new char[escapedBy.length+value.length];
    if (escapedBy.length > 0) {
      for (int i = 0; i < escapedBy.length; ++i) {
        this.escapeSequence[i] = escapedBy[i];
      }
      for (int i = escapedBy.length; i < escapeSequence.length; ++i) {
        this.escapeSequence[i] = value[i-escapedBy.length];
      }
    }
  }

  public final boolean testStart(CharBuffer buffer, CharArrayBuilder charArrayBuilder) {
    return false;
  }

  public final boolean foundEndDelimiter(CharBuffer buffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    //System.out.println("foundEndDelimiter delimiter="+new String(value));
    boolean found = buffer.startsWith(value);
    if (found) {
      //int start = buffer.getPosition(); 
      charArrayBuilder.append(value);
      buffer.next(value.length);
      //System.out.println("EOL found start=" + start + ", end=" + buffer.getPosition() + ", len=" + value.length);
    }
    return found;
  }

  public final int foundEndDelimiter(char[] data, int start, int length) {
    //System.out.println("foundEndDelimiter " + new String(data,start,length) + ", delimiter="+new String(value));
    int index = CharArrayHelper.startsWith(data, start, length, value);
    return index;
  }

  public final int foundEndDelimiter(char[] data, int start, int length, TokenReceiver receiver) {
    //System.out.println("foundEndDelimiter " + new String(data,start,length) + ", delimiter="+new String(value));
    int index = CharArrayHelper.startsWith(data, start, length, value);
    return index;
  }

  public final int testStart(char[] data, int start, int length) {
    return 0;
  }
  public final int readEscapedDelimiter(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) {
    int index = CharArrayHelper.startsWith(data, start, length, escapeSequence);
    //if (data[start] == '?') {
      //System.out.println(getClass().getName()+" Testing for Escaped:" +new String(data,start,length)+" " + new String(value));
    //}
    if (index > 0) {
      charArrayBuilder.append(value);
      //System.out.println(getClass().getName()+" Escaped:" +new String(value));
    }
    return index;
  }

  public final boolean readEscapedDelimiter(CharBuffer buffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean found = buffer.startsWith(escapeSequence);
    if (found) {
      charArrayBuilder.append(value);
      buffer.next(escapeSequence.length);
    }
    return found;
  }

  public final boolean testContinuation(CharBuffer buffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean found = buffer.startsWith(continuationSequence);
    if (found) {
      //System.out.println(getClass().getName()+".lineContinuation found len="+continuationSequence.length + ", " + new String(continuationSequence));
      charArrayBuilder.append(continuationSequence);
      buffer.next(continuationSequence.length);
    }
    return found;
  }

  public String toString() {
    return new String(value);
  }
}

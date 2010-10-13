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
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.ByteArrayHelper;
import com.servingxml.components.flatfile.options.ByteBuffer;

public final class EndByteDelimiterExtractor implements ByteDelimiterExtractor {
  private final byte[] value;
  private final byte[] continuationBytes;
  private final byte[] escapeSequence;

  public EndByteDelimiterExtractor(byte[] value) {
    this.value = value;
    this.continuationBytes = SystemConstants.EMPTY_BYTE_ARRAY;
    this.escapeSequence = SystemConstants.EMPTY_BYTE_ARRAY;
  }

  public EndByteDelimiterExtractor(byte[] value, byte[] continuationSequence) {
    this.value = value;
    this.continuationBytes = continuationSequence;
    this.escapeSequence = SystemConstants.EMPTY_BYTE_ARRAY;
  }

  public final boolean testStart(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) {
    return false;
  }

  public final boolean foundEndDelimiter(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    //System.out.println("foundEndDelimiter delimiter="+new String(value));
    boolean found = recordBuffer.startsWith(value);
    if (found) {
      //int start = recordBuffer.getPosition(); 
      byteArrayBuilder.append(value);
      recordBuffer.next(value.length);
      //System.out.println("EOL found start=" + start + ", end=" + recordBuffer.getPosition() + ", len=" + value.length);
    }
    return found;
  }

  public final int foundEndDelimiter(byte[] data, int start, int length) {
    //System.out.println("foundEndDelimiter " + new String(data,start,length) + ", delimiter="+new String(value));
    int index = ByteArrayHelper.startsWith(data, start, length, value);
    return index;
  }

  public final int testStart(byte[] data, int start, int length) {
    return 0;
  }

  public final boolean testContinuation(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(continuationBytes);
    if (found) {
      byteArrayBuilder.append(continuationBytes);
      recordBuffer.next(continuationBytes.length);
    }
    return found;
  }

  public int readEscapedDelimiter(byte[] data, int start, int length, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    int index = ByteArrayHelper.startsWith(data, start, length, escapeSequence);
    //if (data[start] == '?') {
      //System.out.println(getClass().getName()+" Testing for Escaped:" +new String(data,start,length)+" " + new String(value));
    //}
    if (index > 0) {
      byteArrayBuilder.append(value);
      //System.out.println(getClass().getName()+" Escaped:" +new String(value));
    }
    return index;
  }
}

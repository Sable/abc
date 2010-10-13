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

import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.ByteArrayHelper;
import com.servingxml.util.CharArrayHelper;
import com.servingxml.components.flatfile.options.ByteBuffer;

public final class QuoteSymbolByteCheckerImpl implements QuoteSymbolByteChecker {
  private final byte[] value;
  private final byte[] escapeSequence;

  public QuoteSymbolByteCheckerImpl(byte[] value, byte[] escapeSequence) {
    this.value = value;
    this.escapeSequence = escapeSequence;
  }

  public final boolean foundEscapedQuoteSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(escapeSequence);
    if (found) {
      byteArrayBuilder.append(escapeSequence);
      recordBuffer.next(escapeSequence.length);
    }
    return found;
  }

  public final int foundEscapedQuoteSymbol(byte[] data, int start, int length) 
  throws IOException {
    int n = ByteArrayHelper.startsWith(data, start, length, escapeSequence);
    return n;
  }

  public final boolean foundQuoteSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(value);
    if (found) {
      byteArrayBuilder.append(value);
      recordBuffer.next(value.length);
    }
    return found;
  }

  public final int foundQuoteSymbol(byte[] data, int start, int length) 
  throws IOException {
    return ByteArrayHelper.startsWith(data, start, length, value);
  }

  public int length() {
    return value.length;
  }

  public int startsBuffer(byte[] buffer) {
    return ByteArrayHelper.startsWith(buffer,0,buffer.length,value);
  }

  public int startsBuffer(char[] buffer) {
    return CharArrayHelper.startsWith(buffer,0,buffer.length,new char[]{'c'});
  }
}

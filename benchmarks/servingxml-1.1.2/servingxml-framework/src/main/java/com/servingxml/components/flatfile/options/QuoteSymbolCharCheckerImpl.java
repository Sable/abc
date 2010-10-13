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

import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.CharArrayHelper;
import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.components.flatfile.scanner.characters.CharInput;

public final class QuoteSymbolCharCheckerImpl implements QuoteSymbolCharChecker {
  private final char[] value;
  private final char[] escapeSequence;

  public QuoteSymbolCharCheckerImpl(char[] value, char[] escapeSequence) {
    this.value = value;
    this.escapeSequence = escapeSequence;
  }

  public final boolean foundEscapedQuoteSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(escapeSequence);
    if (found) {
      charArrayBuilder.append(escapeSequence);
      recordBuffer.next(escapeSequence.length);
    }
    return found;
  }

  public final int foundEscapedQuoteSymbol(char[] data, int start, int length) 
  throws IOException {
    int n = CharArrayHelper.startsWith(data, start, length, escapeSequence);
    return n;
  }

  public final int readEscapedQuoteSymbol(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    int n = CharArrayHelper.startsWith(data, start, length, escapeSequence);
    if (n > 0) {
      charArrayBuilder.append(data, start+(n-value.length), value.length);
    }
    return n;
  }

  public final boolean foundQuoteSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(value);
    if (found) {
      charArrayBuilder.append(value);
      recordBuffer.next(value.length);
    }
    return found;
  }

  public final int foundQuoteSymbol(char[] input, int inputOffset, int inputLength) 
  throws IOException {
    return CharArrayHelper.startsWith(input, inputOffset, inputLength, value);
  }

  public int length() {
    return value.length;
  }
}

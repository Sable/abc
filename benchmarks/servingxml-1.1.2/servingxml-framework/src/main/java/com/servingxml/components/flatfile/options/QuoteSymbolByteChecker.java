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
import com.servingxml.components.flatfile.options.ByteBuffer;

public interface QuoteSymbolByteChecker {
  public final static QuoteSymbolByteChecker NULL = new NullQuoteSymbolChecker();

  boolean foundEscapedQuoteSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException;

  int foundEscapedQuoteSymbol(byte[] data, int start, int length) 
  throws IOException;

  boolean foundQuoteSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException;

  int foundQuoteSymbol(byte[] data, int start, int length) 
  throws IOException;

  int length();
}

final class NullQuoteSymbolChecker implements QuoteSymbolByteChecker {
  public final boolean foundEscapedQuoteSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int foundEscapedQuoteSymbol(byte[] data, int start, int length) 
  throws IOException {
    return 0;
  }

  public final boolean foundQuoteSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int foundQuoteSymbol(byte[] data, int start, int length) 
  throws IOException {
    return 0;
  }

  public int length() {
    return 0;
  }
  public int startsBuffer(byte[] buffer) {
    return 0;
  }
  public int startsBuffer(char[] buffer) {
    return 0;
  }
}


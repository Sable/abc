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

import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.components.flatfile.scanner.characters.CharInput;
import com.servingxml.util.CharArrayBuilder;

public interface QuoteSymbolCharChecker {
  public final static QuoteSymbolCharChecker NULL = new NullCharQuoteSymbolChecker();

  boolean foundEscapedQuoteSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException;

  int foundEscapedQuoteSymbol(char[] data, int start, int length) 
  throws IOException;

  int readEscapedQuoteSymbol(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) 
  throws IOException;

  boolean foundQuoteSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException;

  int foundQuoteSymbol(char[] data, int start, int length) 
  throws IOException;

  int length();
}

final class NullCharQuoteSymbolChecker implements QuoteSymbolCharChecker {
  public final boolean foundEscapedQuoteSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int foundEscapedQuoteSymbol(char[] data, int start, int length) 
  throws IOException {
    return 0;
  }

  public final int readEscapedQuoteSymbol(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return 0;
  }

  public final boolean foundQuoteSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int foundQuoteSymbol(char[] data, int start, int length) 
  throws IOException {
    return 0;
  }

  public int length() {
    return 0;
  }
}


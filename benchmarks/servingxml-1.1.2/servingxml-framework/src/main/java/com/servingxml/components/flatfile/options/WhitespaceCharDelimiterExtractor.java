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
import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.components.flatfile.parsing.TokenReceiver;

public final class WhitespaceCharDelimiterExtractor implements CharDelimiterExtractor {
  private static CharTrimmer charTrimmer;

  public WhitespaceCharDelimiterExtractor(CharTrimmer charTrimmer) {
    this.charTrimmer = charTrimmer;
  }

  public final boolean testStart(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) {
    return false;
  }

  public final boolean foundEndDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return charTrimmer.checkWhitespace(recordBuffer,charArrayBuilder);
  }

  public final boolean readEscapedDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return false;
  }
  public final int readEscapedDelimiter(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) {
    return 0;
  }

  public final boolean testContinuation(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int foundEndDelimiter(char[] data, int start, int length) {
    return charTrimmer.countLeadingWhitespace(data, start, length);
  }

  public final int foundEndDelimiter(char[] data, int start, int length, TokenReceiver receiver) {
    return charTrimmer.countLeadingWhitespace(data, start, length);
  }

  public final int testStart(char[] data, int start, int length) {
    return 0;
  }

  public final static WhitespaceCharDelimiterExtractor newInstance() {
    CharTrimmer charTrimmer = CharTrimmer.newInstance();

    return new WhitespaceCharDelimiterExtractor(charTrimmer);
  }
}

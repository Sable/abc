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

public interface CharDelimiterExtractor {
  public static final CharDelimiterExtractor NULL = new NullCharDelimiterExtractor();
  public static final CharDelimiterExtractor[] EMPTY_ARRAY = new CharDelimiterExtractor[0];

  boolean testStart(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException;
  boolean foundEndDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException;
  boolean readEscapedDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException;
  boolean testContinuation(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException;
  int foundEndDelimiter(char[] data, int start, int length) 
  throws IOException;
  int foundEndDelimiter(char[] data, int start, int length, TokenReceiver receiver);
  int readEscapedDelimiter(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) 
  throws IOException;
  int testStart(char[] data, int start, int length) 
  throws IOException;
}

final class NullCharDelimiterExtractor implements CharDelimiterExtractor {
  public final boolean testStart(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) {
    return false;
  }
  public final boolean foundEndDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) {
    return false;
  }
  public final boolean readEscapedDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) {
    return false;
  }
  public final boolean testContinuation(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) {
    return false;
  }

  public final int foundEndDelimiter(char[] data, int start, int length) {
    return 0;
  }

  public final int foundEndDelimiter(char[] data, int start, int length, TokenReceiver receiver) {
    return 0;
  }

  public final int readEscapedDelimiter(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) {
    //if (data[0] == '?') {
      //System.out.println(getClass().getName()+" Testing for Escaped:" +new String(data));
    //}
    return 0;
  }
  public int testStart(char[] data, int start, int length) 
  {
    return 0;
  }
}

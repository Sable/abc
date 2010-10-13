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
import com.servingxml.components.flatfile.parsing.TokenReceiver;

public class StartEndCharDelimiterExtractor implements CharDelimiterExtractor {

  private final char[] startValue;
  private final char[] endValue;

  public StartEndCharDelimiterExtractor(char[] startValue, char[] endValue) {
    this.startValue = startValue;
    this.endValue = endValue;
  }

  public boolean testStart(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(startValue);
    if (found) {
      charArrayBuilder.append(startValue);
      recordBuffer.next(startValue.length);
    }
    return found;
  }

  public boolean foundEndDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(endValue);
    if (found) {
      charArrayBuilder.append(endValue);
      recordBuffer.next(endValue.length);
    }
    return found;
  }

  public final int testStart(char[] data, int start, int length) {
    int index = CharArrayHelper.startsWith(data, start, length, startValue);
    return index;
  }

  public final int foundEndDelimiter(char[] data, int start, int length) {
    int index = CharArrayHelper.startsWith(data, start, length, endValue);
    return index;
  }

  public final int foundEndDelimiter(char[] data, int start, int length, TokenReceiver receiver) {
    int index = CharArrayHelper.startsWith(data, start, length, endValue);
    return index;
  }

  public final int readEscapedDelimiter(char[] data, int start, int length, CharArrayBuilder charArrayBuilder) {
    return 0;
  }

  public final boolean readEscapedDelimiter(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return false;
  }

  public final boolean testContinuation(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    return false;
  }
}

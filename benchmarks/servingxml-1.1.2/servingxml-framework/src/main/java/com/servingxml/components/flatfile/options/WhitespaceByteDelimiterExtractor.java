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
import java.nio.charset.Charset;

import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.components.flatfile.options.ByteBuffer;

public final class WhitespaceByteDelimiterExtractor implements ByteDelimiterExtractor {
  private static ByteTrimmer byteTrimmer;

  public WhitespaceByteDelimiterExtractor(ByteTrimmer byteTrimmer) {
    this.byteTrimmer = byteTrimmer;
  }

  public final boolean testStart(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) {
    return false;
  }

  public final boolean foundEndDelimiter(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    return byteTrimmer.checkWhitespace(recordBuffer,byteArrayBuilder);
  }

  public final boolean testContinuation(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int foundEndDelimiter(byte[] data, int start, int length) {
    return byteTrimmer.countLeadingWhitespace(data, start, length);
  }

  public final int testStart(byte[] data, int start, int length) {
    return 0;
  }

  public final int readEscapedDelimiter(byte[] data, int start, int length, ByteArrayBuilder byteArrayBuilder) {
    return 0;
  }

  public final static WhitespaceByteDelimiterExtractor newInstance(Charset charset) {
    ByteTrimmer byteTrimmer = ByteTrimmer.newInstance(charset);

    return new WhitespaceByteDelimiterExtractor(byteTrimmer);
  }
}

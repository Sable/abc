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

import com.servingxml.components.flatfile.options.ByteBuffer;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.ByteArrayHelper;

public class StartEndByteDelimiterExtractor implements ByteDelimiterExtractor {

  private final byte[] startValue;
  private final byte[] endValue;

  public StartEndByteDelimiterExtractor(byte[] startValue, byte[] endValue) {
    this.startValue = startValue;
    this.endValue = endValue;
  }

  public boolean testStart(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(startValue);
    if (found) {
      byteArrayBuilder.append(startValue);
      recordBuffer.next(startValue.length);
    }
    return found;
  }

  public boolean foundEndDelimiter(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    boolean found = recordBuffer.startsWith(endValue);
    if (found) {
      byteArrayBuilder.append(endValue);
      recordBuffer.next(endValue.length);
    }
    return found;
  }

  public final int testStart(byte[] data, int start, int length) {
    int index = ByteArrayHelper.startsWith(data, start, length, startValue);
    return index;
  }

  public final int foundEndDelimiter(byte[] data, int start, int length) {
    int index = ByteArrayHelper.startsWith(data, start, length, endValue);
    return index;
  }

  public final boolean testContinuation(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    return false;
  }

  public final int readEscapedDelimiter(byte[] data, int start, int length, ByteArrayBuilder byteArrayBuilder) {
    return 0;
  }
}

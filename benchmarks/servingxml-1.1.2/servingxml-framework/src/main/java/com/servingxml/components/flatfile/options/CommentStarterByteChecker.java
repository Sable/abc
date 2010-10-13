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
import com.servingxml.util.CharsetHelper;
import com.servingxml.components.flatfile.options.ByteBuffer;

public abstract class CommentStarterByteChecker {
  public final static CommentStarterByteChecker NULL = new NullCommentStarterChecker();

  public abstract boolean checkCommentSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException;

  public static CommentStarterByteChecker newInstance(String commentStarterString, Charset charset) {
    byte[] value = CharsetHelper.stringToBytes(commentStarterString, charset);
    CommentStarterByteChecker checker;
    if (value.length == 1) {
      checker = new CommentStarterChecker1(value[0]);
    } else {
      checker = new CommentStarterCheckerN(value);
    }
    return checker;
  }

  static final class CommentStarterCheckerN extends CommentStarterByteChecker {
    private final byte[] value;

    public CommentStarterCheckerN(byte[] value) {
      this.value = value;
    }

    public final boolean checkCommentSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {
      boolean found = recordBuffer.startsWith(value);
      if (found) {
        byteArrayBuilder.append(value);
        recordBuffer.next(value.length);
      }
      return found;
    }

  }

  static final class CommentStarterChecker1 extends CommentStarterByteChecker {
    private final byte value;

    public CommentStarterChecker1(byte value) {
      this.value = value;
    }

    public final boolean checkCommentSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {
      boolean found = recordBuffer.current() == value;
      if (found) {
        byteArrayBuilder.append(value);
        recordBuffer.next(1);
      }
      return found;
    }

  }

  static final class NullCommentStarterChecker extends CommentStarterByteChecker {
    public final boolean checkCommentSymbol(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {
      return false;
    }
  }
}



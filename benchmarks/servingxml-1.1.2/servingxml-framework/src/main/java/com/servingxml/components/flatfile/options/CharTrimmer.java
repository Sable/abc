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

import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.CharArrayHelper;
import com.servingxml.util.ServingXmlException;

public abstract class CharTrimmer {

  public abstract void writeTo(RecordOutput recordOutput);

  public abstract boolean checkSpace(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder)
  throws IOException;

  public abstract boolean checkWhitespace(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder)
  throws IOException;

  public abstract int countLeadingWhitespace(char[] data, int start, int length);

  public boolean isAllWhitespace(char[] data, int start, int length) {
    int count = countLeadingWhitespace(data, start, length);
    return count == length;
  }

  public abstract int countTrailingWhitespace(char[] data, int start, int length);

  public static CharTrimmer newInstance() {

    CharTrimmer checker = new WhitespaceChecker1();

    return checker;
  }

  static class WhitespaceChecker1 extends CharTrimmer {
    private final char spaceByte;
    private final char tabByte;

    public WhitespaceChecker1() {
      this.spaceByte = ' ';
      this.tabByte = '\t';
    }

    public final void writeTo(RecordOutput recordOutput) {
      recordOutput.writeString(" ");
    }

    public final boolean checkSpace(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
    throws IOException {

      //System.out.println("WhitespaceChecker1.checkSpace");
      boolean found = false;
      boolean done = false;
      while (!done && !recordBuffer.done()) {
        char b = recordBuffer.current();
        //System.out.print(new String(new char[]{b}) + ",");
        boolean result = (b == spaceByte || b == tabByte);
        if (result) {
          charArrayBuilder.append(b);
          recordBuffer.next();
          found = true;
        } else {
          done = true;
        }
      }
      return found;
    }

    public final boolean checkWhitespace(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
    throws IOException {
      boolean found = false;
      boolean done = false;
      while (!done && !recordBuffer.done()) {
        char b = recordBuffer.current();
        boolean result = Character.isWhitespace(b);
        if (result) {
          charArrayBuilder.append(b);
          recordBuffer.next();
          found = true;
        } else {
          done = true;
        }
      }
      return found;
    }

    public final int countLeadingWhitespace(char[] data, int start, int length) {
      int index = 0;
      boolean done = false;
      while (!done && index < length) {
        char b = data[start+index];
        if (Character.isWhitespace(b)) {
          ++index;
        } else {
          done = true;
        }
      }

      return index;
    }

    public final int countTrailingWhitespace(char[] data, int start, int length) {

      int index = length;
      boolean done = false;
      while (!done && index > 0) {
        char b = data[start+index-1];
        if (Character.isWhitespace(b)) {
          --index;
        } else {
          done = true;
        }
      }

      return length - index;
    }
  }

}

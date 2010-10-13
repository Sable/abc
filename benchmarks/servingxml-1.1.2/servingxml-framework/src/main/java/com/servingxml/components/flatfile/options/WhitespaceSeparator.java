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

import java.nio.charset.Charset;

import com.servingxml.components.flatfile.RecordOutput;

public class WhitespaceSeparator implements Separator {

  public WhitespaceSeparator() {
  }

  public boolean occursIn(String s) {
    boolean isContained = false;
    for (int i = 0; !isContained && i < s.length(); ++i) {
      char c = s.charAt(i);
      if (Character.isWhitespace(c)) {
        isContained = true;
      }
    }
    return isContained;
  }

  public boolean equalsString(String s) {
    boolean isEqual = false;
    if (s.length() > 0) {
      isEqual = true;
      for (int i = 0; isEqual && i < s.length(); ++i) {
        char c = s.charAt(i);
        if (!Character.isWhitespace(c)) {
          isEqual = false;
        }
      }
    }
    return isEqual;
  }

  public ByteDelimiterExtractor createByteDelimiterExtractor(Charset charset) {

    return WhitespaceByteDelimiterExtractor.newInstance(charset);
  }

  public CharDelimiterExtractor createCharDelimiterExtractor() {

    return WhitespaceCharDelimiterExtractor.newInstance();
  }

  public boolean isEmpty() {
    return false;
  }

  public String toString() {
    return " ";
  }

  public void writeEndDelimiterTo(StringBuilder buf) {
    buf.append(" ");
  }               

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
    recordOutput.writeString(" ");
  }
}

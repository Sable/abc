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

import com.servingxml.util.StringHelper;
import com.servingxml.util.CharsetHelper;
import com.servingxml.components.flatfile.RecordOutput;

public class StartEndSeparator implements Separator {
  
  private final char[] startValue;
  private final char[] endValue;

  public StartEndSeparator(String startValue, String endValue) {
    this.startValue = startValue.toCharArray();
    this.endValue = endValue.toCharArray();
  }

  public boolean occursIn(String s) {
    boolean isContained = StringHelper.contains(s, startValue) || StringHelper.contains(s, endValue);
    return isContained;
  }

  public ByteDelimiterExtractor createByteDelimiterExtractor(Charset charset) {
    byte[] rawStart = CharsetHelper.charactersToBytes(startValue, charset);
    byte[] rawEnd = CharsetHelper.charactersToBytes(endValue, charset);

    return new StartEndByteDelimiterExtractor(rawStart, rawEnd);
  }

  public CharDelimiterExtractor createCharDelimiterExtractor() {
    return new StartEndCharDelimiterExtractor(startValue, endValue);
  }

  public boolean isEmpty() {
    return startValue.length == 0;
  }

  public boolean equalsString(String s) {

    return false;
  }

  public void writeEndDelimiterTo(StringBuilder buf) {
    buf.append(endValue,0,endValue.length);
  }               

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
    recordOutput.writeCharacters(endValue);
  }               

  public String toString() {
    return new String(startValue) + new String(endValue);
  }
}

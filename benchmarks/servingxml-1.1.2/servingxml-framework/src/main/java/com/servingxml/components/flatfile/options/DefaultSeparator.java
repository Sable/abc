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
import com.servingxml.util.SystemConstants;
import com.servingxml.util.CharsetHelper;

import com.servingxml.components.flatfile.RecordOutput;

public class DefaultSeparator implements Separator {
  
  private static final char[] comma = new char[]{','};
  public static final DefaultSeparator COMMA = new DefaultSeparator(comma);

  private final char[] value;
  private final char[] escapedBy;
  private final char[] continuationSequence;

  public DefaultSeparator() {
    this.value = System.getProperty("line.separator").toCharArray();
    this.escapedBy = SystemConstants.EMPTY_CHAR_ARRAY;
    this.continuationSequence = SystemConstants.EMPTY_CHAR_ARRAY;
  }

  public DefaultSeparator(char[] value) {
    this.value = value;
    this.escapedBy = SystemConstants.EMPTY_CHAR_ARRAY;
    this.continuationSequence = SystemConstants.EMPTY_CHAR_ARRAY;
  }

  public DefaultSeparator(String s) {
    this.value = s.toCharArray();
    this.escapedBy = SystemConstants.EMPTY_CHAR_ARRAY;
    this.continuationSequence = SystemConstants.EMPTY_CHAR_ARRAY;
  }

  public DefaultSeparator(String s, String escapedBy, String continuationSequence) {
    this.value = s.toCharArray();
    this.escapedBy = escapedBy.toCharArray();
    this.continuationSequence = continuationSequence.toCharArray();
  }

  public void writeEndDelimiterTo(StringBuilder buf) {
    //System.out.println(getClass().getName()+".writeEndDelimiterTo buf " + toString());
    buf.append(value,0,value.length);
  }               

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
    //System.out.println(getClass().getName()+".writeEndDelimiterTo recordOutput " + toString());
    recordOutput.writeCharacters(value);
  }               

  public String toString() {
    return new String(value);
  }

  public ByteDelimiterExtractor createByteDelimiterExtractor(Charset charset) {
    byte[] rawDelimiter = CharsetHelper.charactersToBytes(value, charset);
    byte[] rawContinuation = CharsetHelper.charactersToBytes(continuationSequence, charset);

    return new EndByteDelimiterExtractor(rawDelimiter, rawContinuation);
  }

  public CharDelimiterExtractor createCharDelimiterExtractor() {
    return new EndCharDelimiterExtractor(value, escapedBy, continuationSequence);
  }

  public boolean occursIn(String s) {

    boolean isContained = StringHelper.contains(s, value) || 
      StringHelper.contains(s, continuationSequence);
    return isContained;
  }

  public boolean equalsString(String s) {

    boolean isEqual = s.length() == value.length && StringHelper.contains(s, value);
    return isEqual;
  }

  public boolean isEmpty() {
    return value.length == 0;
  }
}

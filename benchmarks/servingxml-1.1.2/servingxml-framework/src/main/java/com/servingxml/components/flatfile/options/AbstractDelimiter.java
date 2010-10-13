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

import java.lang.reflect.Array;
import java.nio.charset.Charset;

import com.servingxml.components.flatfile.RecordOutput;

public abstract class AbstractDelimiter implements Delimiter {

  protected final Separator separator;
  private final boolean reading;
  private final boolean writing;

  public AbstractDelimiter(char[] symbol, boolean reading, boolean writing) {
    this.separator = new DefaultSeparator(symbol);
    this.reading = reading;
    this.writing = writing;
  }

  public AbstractDelimiter(String symbol, boolean reading, boolean writing) {
    this.separator = new DefaultSeparator(symbol);
    this.reading = reading;
    this.writing = writing;
  }

  public AbstractDelimiter(String symbol, String escapedBy, String continuationSequence, boolean reading, boolean writing) {
    this.separator = new DefaultSeparator(symbol, escapedBy, continuationSequence);
    this.reading = reading;
    this.writing = writing;
  }

  public AbstractDelimiter(Separator separator, boolean reading, boolean writing) {
    this.separator = separator;
    this.reading = reading;
    this.writing = writing;
  }

  public AbstractDelimiter(String startValue, String endValue) {
    this.separator = new StartEndSeparator(startValue, endValue);
    this.reading = true;
    this.writing = true;
  }

  public void writeEndDelimiterTo(StringBuilder buf) {
    separator.writeEndDelimiterTo(buf);
  }

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
    separator.writeEndDelimiterTo(recordOutput);
  }

  public boolean isEmpty() {
    return separator.isEmpty();
  }

  public String toString() {
    return separator.toString();
  }

  protected static Object trimArray(Delimiter[] delimiters) {
    int count = 0;
    for (int i = 0; i < delimiters.length; ++i) {
      if (delimiters[i].isEmpty()) {
        ++count;
      }
    }
    Object newDelimiters = delimiters;
    if (count > 0) {
      newDelimiters = Array.newInstance(delimiters.getClass().getComponentType(),delimiters.length - count);
      int j = 0;
      for (int i = 0; i < delimiters.length; ++i) {
        if (!delimiters[i].isEmpty()) {
          Array.set(newDelimiters, j++, delimiters[i]);
        }
      }
    }

    return newDelimiters;
  }

  public boolean occursIn(String s) {
    return separator.occursIn(s);
  }

  public ByteDelimiterExtractor createByteDelimiterExtractor(Charset charset) {
    return separator.createByteDelimiterExtractor(charset);
  }

  public CharDelimiterExtractor createCharDelimiterExtractor() {
    return separator.createCharDelimiterExtractor();
  }

  public DelimiterExtractor createDelimiterExtractor(Charset charset) {
    return new DelimiterExtractor(separator.createByteDelimiterExtractor(charset),
                                  separator.createCharDelimiterExtractor());
  }

  public boolean equalsString(String s) {
    return separator.equalsString(s);
  }

  public boolean forReading() {
    return reading;
  }

  public boolean forWriting() {
    return writing;
  }
}

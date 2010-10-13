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

package com.servingxml.components.flatfile.scanner.characters;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.CharDelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.QuoteSymbolCharChecker;
import com.servingxml.components.flatfile.options.CharTrimmer;
import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;

public class CharInput {
  private final char[] input;
  private int start;
  private final int length;
  private final Charset charset;

  public CharInput(char[] input, int start, int length, Charset charset) {
    this.input = input;
    this.start = start;
    this.length = length;
    this.charset = (charset == null) ? Charset.defaultCharset() : charset;
  }

  public CharInput(char[] input, int start, int length) {
    this.input = input;
    this.start = start;
    this.length = length;
    this.charset = Charset.defaultCharset();
  }

  public CharInput(char[] input, Charset charset) {
    this.input = input;
    this.start = 0;
    this.length = input.length;
    this.charset = (charset == null) ? Charset.defaultCharset() : charset;
  }

  public boolean empty() {
    return start >= length;
  }

  public char charAt(int position) {
    return input[start+position];
  }

  public void reset(int position) {
    this.start = position;
  }
}





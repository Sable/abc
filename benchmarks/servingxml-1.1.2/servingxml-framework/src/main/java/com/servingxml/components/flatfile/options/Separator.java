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

public interface Separator {
  public static final Separator WHITESPACE = new WhitespaceSeparator();

  public static final Separator[] EMPTY_ARRAY = new Separator[0];

  void writeEndDelimiterTo(StringBuilder buf);

  void writeEndDelimiterTo(RecordOutput recordOutput);

  ByteDelimiterExtractor createByteDelimiterExtractor(Charset charset);

  CharDelimiterExtractor createCharDelimiterExtractor();

  boolean occursIn(String s);

  boolean equalsString(String s);

  boolean isEmpty();
}

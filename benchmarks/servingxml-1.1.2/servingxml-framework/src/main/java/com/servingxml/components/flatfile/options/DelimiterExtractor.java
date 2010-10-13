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

public class DelimiterExtractor {
  public static final DelimiterExtractor NULL = new DelimiterExtractor(ByteDelimiterExtractor.NULL,
                                                                       CharDelimiterExtractor.NULL);
  public static final DelimiterExtractor[] EMPTY_ARRAY = new DelimiterExtractor[0];

  private final ByteDelimiterExtractor byteDelimiterExtractor;
  private final CharDelimiterExtractor charDelimiterExtractor;

  public DelimiterExtractor(ByteDelimiterExtractor byteDelimiterExtractor,
                            CharDelimiterExtractor charDelimiterExtractor) {
    this.byteDelimiterExtractor = byteDelimiterExtractor;
    this.charDelimiterExtractor = charDelimiterExtractor;
  }

  public ByteDelimiterExtractor getByteDelimiterExtractor() {return byteDelimiterExtractor; }

  public CharDelimiterExtractor getCharDelimiterExtractor() {return charDelimiterExtractor; }
}



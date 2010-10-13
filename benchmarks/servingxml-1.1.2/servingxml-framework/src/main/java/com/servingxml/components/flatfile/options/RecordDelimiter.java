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


public class RecordDelimiter extends AbstractDelimiter {

  private static final char[] CRLF_SEQ = new char[]{'\r','\n'};
  private static final char[] LF_SEQ = new char[]{'\n'};

  public static final RecordDelimiter[] EMPTY_ARRAY = new RecordDelimiter[0];
  
  public static final RecordDelimiter SYSTEM = new RecordDelimiter();
  public static final RecordDelimiter CRLF = new RecordDelimiter(CRLF_SEQ, true, true);
  public static final RecordDelimiter LF = new RecordDelimiter(LF_SEQ, true, true);
  
  public RecordDelimiter() {
    super(System.getProperty("line.separator"), true, true);
  }

  public RecordDelimiter(char[] values, boolean reading, boolean writing) {            
    super(values, reading, writing);
  }
                                                                                                      
  public RecordDelimiter(String s, boolean reading, boolean writing) {
    super(s, reading, writing);
  }

  public RecordDelimiter(String s) {
    super(s, true, true);
  }

  public RecordDelimiter(String startValue, String endValue) {
    super(startValue, endValue);
  }

  public RecordDelimiter(Separator separator, boolean reading, boolean writing) {
    super(separator, reading, writing);
  }

  public static RecordDelimiter[] trimArray(RecordDelimiter[] delimiters) {
    return (RecordDelimiter[])AbstractDelimiter.trimArray(delimiters);
  }
}

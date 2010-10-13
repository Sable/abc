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

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

public class RecordDelimiterFactory implements DelimiterFactory {
  private static final RecordDelimiter CRLF = RecordDelimiter.CRLF;
  private static final RecordDelimiter LF = RecordDelimiter.LF;

  public static final DelimiterFactory[] DEFAULT = new DelimiterFactory[] {
    new SimpleDelimiterFactory(CRLF),
    new SimpleDelimiterFactory(LF)
  };
  public static final DelimiterFactory[] EMPTY_ARRAY = new DelimiterFactory[0];

  private final SeparatorFactory separatorFactory;
  private final boolean reading;
  private final boolean writing;

  public RecordDelimiterFactory(SeparatorFactory separatorFactory, boolean reading, boolean writing) {
    this.separatorFactory = separatorFactory;
    this.reading = reading;
    this.writing = writing;
  }

  public Delimiter createDelimiter(ServiceContext context, Flow flow) {
    Separator separator = separatorFactory.createSeparator(context, flow);

    return new RecordDelimiter(separator, reading, writing);
  }
}

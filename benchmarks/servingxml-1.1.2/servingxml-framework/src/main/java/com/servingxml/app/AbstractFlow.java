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

package com.servingxml.app;

import com.servingxml.util.record.Record;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.saxsource.RecordSaxSource;
import com.servingxml.io.saxsink.SaxSink;

/**
 *
 * 
 * @author  Daniel A. Parker             
 */

public abstract class AbstractFlow extends FlowModifier {

  private final Flow tail;

  public AbstractFlow(Flow tail) {
    this.tail = tail;
  }

  public Record getParameters() {
    return tail.getParameters();
  }

  public Record getRecord() {
    return tail.getRecord();
  }

  public int getCurrentLineNumber() {
    return tail.getCurrentLineNumber();
  }

  public StreamSource getDefaultStreamSource() {
    return tail.getDefaultStreamSource();
  }

  public SaxSource getDefaultSaxSource() {
    return tail.getDefaultSaxSource();
  }

  public StreamSink getDefaultStreamSink() {
    return tail.getDefaultStreamSink();
  }

  public SaxSink getDefaultSaxSink() {
    return tail.getDefaultSaxSink();
  }
}

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
import com.servingxml.io.saxsource.DefaultSaxSource;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.saxsource.RecordSaxSource;
import com.servingxml.io.saxsink.SaxSink;

/**
 *
 * 
 * @author  Daniel A. Parker             
 */

public class FlowImpl extends FlowModifier {
  private final Record parameters;
  private final StreamSource defaultStreamSource;
  private final SaxSource defaultSaxSource;
  private final Record record;
  private final StreamSink defaultStreamSink;
  private final SaxSink defaultSaxSink;
  private final int currentLineNumber;

  public FlowImpl() {
    this.defaultStreamSource = StreamSource.NULL;
    this.defaultSaxSource = new DefaultSaxSource();
    this.defaultStreamSink = StreamSink.NULL;
    this.defaultSaxSink = SaxSink.NULL;
    this.record = Record.EMPTY;
    this.currentLineNumber = 0;
    this.parameters = Record.EMPTY;
  }

  public FlowImpl(Record parameters) {
    this.defaultStreamSource = StreamSource.NULL;
    this.defaultSaxSource =  new DefaultSaxSource();
    this.defaultStreamSink = StreamSink.NULL;
    this.defaultSaxSink = SaxSink.NULL;
    this.record = Record.EMPTY;
    this.currentLineNumber = 0;
    this.parameters = parameters;
  }

  public FlowImpl(Record parameters, 
    StreamSource defaultStreamSource, StreamSink defaultStreamSink) {
    this.defaultStreamSource = defaultStreamSource;
    this.defaultSaxSource = new DefaultSaxSource();
    this.defaultStreamSink = defaultStreamSink;
    this.defaultSaxSink = SaxSink.NULL;
    this.record = Record.EMPTY;
    this.currentLineNumber = 0;
    this.parameters = parameters;
  }

  public FlowImpl(Record parameters, 
    StreamSource defaultStreamSource, StreamSink defaultStreamSink, SaxSource defaultSaxSource) {
    this.defaultStreamSource = defaultStreamSource;
    this.defaultStreamSink = defaultStreamSink;
    this.defaultSaxSink = SaxSink.NULL;
    this.defaultSaxSource = defaultSaxSource;
    this.record = Record.EMPTY;
    this.currentLineNumber = 0;
    this.parameters = parameters;
  }

  public FlowImpl(Environment env, ServiceContext context, Record parameters, Record record) {
    this.defaultStreamSource = StreamSource.NULL;
    this.defaultStreamSink = StreamSink.NULL;
    this.defaultSaxSink = SaxSink.NULL;
    this.defaultSaxSource = new RecordSaxSource(env.getQnameContext().getPrefixMap(), record, context.getTransformerFactory());
    this.record = record;
    this.currentLineNumber = 0;
    this.parameters = parameters;
  }

  public Record getRecord() {
    return record;
  }

  public int getCurrentLineNumber() {
    return currentLineNumber;
  }

  public StreamSource getDefaultStreamSource() {
    return defaultStreamSource;
  }

  public SaxSource getDefaultSaxSource() {
    return defaultSaxSource;
  }

  public StreamSink getDefaultStreamSink() {
    return defaultStreamSink;
  }

  public SaxSink getDefaultSaxSink() {
    return defaultSaxSink;
  }

  public Record getParameters() {
    return parameters;
  }
}

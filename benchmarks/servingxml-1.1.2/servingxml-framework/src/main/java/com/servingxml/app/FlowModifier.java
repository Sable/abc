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
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.saxsource.RecordSaxSource;
import com.servingxml.util.record.RecordBuilder;

/**
 *
 * 
 * @author  Daniel A. Parker             
 */

public abstract class FlowModifier extends Flow {

  public Flow replaceParameters(ServiceContext context, Record parameters) {
    return new ModifyParameters(parameters, this);
  }

  public Flow augmentParameters(ServiceContext context, ParameterDescriptor[] parameterDescriptors) {

    Flow newFlow = this;
    if (parameterDescriptors.length > 0) {
      RecordBuilder parameterBuilder = new RecordBuilder(getParameters());
      for (int i = 0; i < parameterDescriptors.length; ++i) {
        ParameterDescriptor descriptor = parameterDescriptors[i];
        //System.out.println(getClass().getName()+".augmentParameter Augmenting parameter " + descriptor.getName());
        descriptor.addParametersTo(context, this, parameterBuilder);
      }
      Record newParameters = parameterBuilder.toRecord();
      newFlow = replaceParameters(context, newParameters);
    }

    return newFlow;
  }

  public Flow replaceRecord(ServiceContext context, Record record) {
    SaxSource saxSource = new RecordSaxSource(context.getAppContext().getResources().getQnameContext().getPrefixMap(), 
                                              record, 
                                              context.getTransformerFactory());
    return new ModifyCurrentRecord(record, saxSource, this);
  }

  public Flow replaceRecord(ServiceContext context, Record record, int lineNumber) {
    SaxSource saxSource = new RecordSaxSource(context.getAppContext().getResources().getQnameContext().getPrefixMap(), 
                                              record, context.getTransformerFactory());
    return new ModifyCurrentRecord(record, saxSource, lineNumber, this);
  }

  public Flow replaceDefaultStreamSink(ServiceContext context, StreamSink streamSink) {
    return new ModifyDefaultStreamSink(streamSink, this);
  }

  public Flow replaceDefaultSaxSink(ServiceContext context, SaxSink saxSink) {
    return new ModifyDefaultSaxSink(saxSink, this);
  }

  public Flow replaceDefaultSaxSource(ServiceContext context, SaxSource saxSource) {
    return new ModifyDefaultSaxSource(saxSource, this);
  }

  public Flow replaceDefaultStreamSource(ServiceContext context, StreamSource streamSource) {
    return new ModifyDefaultStreamSource(streamSource, this);
  }

  static final class ModifyCurrentRecord extends AbstractFlow {
    private final Record currentRecord;
    private final SaxSource saxSource;
    private final int currentLineNumber;

    ModifyCurrentRecord(Record currentRecord, SaxSource saxSource, Flow tail) {
      super(tail);
      this.currentRecord = currentRecord;
      this.saxSource = saxSource;
      this.currentLineNumber = tail.getCurrentLineNumber();
    }

    ModifyCurrentRecord(Record currentRecord, SaxSource saxSource, int currentLineNumber, Flow tail) {
      super(tail);
      this.currentRecord = currentRecord;
      this.saxSource = saxSource;
      this.currentLineNumber = currentLineNumber;
    }

    public final Record getRecord() {
      return currentRecord;
    }

    public final SaxSource getDefaultSaxSource() {
      return saxSource;
    }

    public final int getCurrentLineNumber() {
      return currentLineNumber;
    }
  }

  static final class ModifyParameters extends AbstractFlow {
    private final Record parameters;

    ModifyParameters(Record parameters, Flow tail) {
      super(tail);
      this.parameters = parameters;
    }

    public final Record getParameters() {
      return parameters;
    }
  }

  static final class ModifyDefaultStreamSink extends AbstractFlow {
    private final StreamSink defaultSink;

    ModifyDefaultStreamSink(StreamSink defaultSink, Flow tail) {
      super(tail);
      this.defaultSink = defaultSink;
    }

    public StreamSink getDefaultStreamSink() {
      return defaultSink;
    }
  }

  static final class ModifyDefaultSaxSink extends AbstractFlow {
    private final SaxSink defaultSink;

    ModifyDefaultSaxSink(SaxSink defaultSink, Flow tail) {
      super(tail);
      this.defaultSink = defaultSink;
    }

    public SaxSink getDefaultSaxSink() {
      return defaultSink;
    }
  }

  static final class ModifyDefaultSaxSource extends AbstractFlow {
    private final SaxSource saxSource;

    ModifyDefaultSaxSource(SaxSource saxSource, Flow tail) {
      super(tail);
      this.saxSource = saxSource;
    }

    public SaxSource getDefaultSaxSource() {
      return saxSource;
    }
  }

  static final class ModifyDefaultStreamSource extends AbstractFlow {
    private final StreamSource defaultStreamSource;

    ModifyDefaultStreamSource(StreamSource defaultStreamSource, Flow tail) {
      super(tail);
      this.defaultStreamSource = defaultStreamSource;
    }

    public StreamSource getDefaultStreamSource() {
      return defaultStreamSource;
    }
  }
}

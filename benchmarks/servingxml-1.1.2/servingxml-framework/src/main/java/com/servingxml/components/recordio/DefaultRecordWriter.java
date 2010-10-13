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

package com.servingxml.components.recordio;

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.IOException;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.Name;
import com.servingxml.app.Flow;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.io.streamsink.StreamSink;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class DefaultRecordWriter extends AbstractRecordWriter implements RecordWriter {
  private final StreamSinkFactory sinkFactory;
  private OutputStream outputStream = null;
  private PrintStream printStream = null;
  private StreamSink sink = null;
  private int recordCount = 0;

  public DefaultRecordWriter(StreamSinkFactory sinkFactory) {
    this.sinkFactory = sinkFactory;
  }

  public final void startRecordStream(ServiceContext context, Flow flow) {
    try {
      sink = sinkFactory.createStreamSink(context,flow);

      outputStream = new BufferedOutputStream(sink.getOutputStream());

      if (sink.getCharset() == null) {
        printStream = new PrintStream(outputStream);
      } else {
        printStream = new PrintStream(outputStream, false, sink.getCharset().name());
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public final void endRecordStream(ServiceContext context, Flow flow) {
  }

  public final void writeRecord(ServiceContext context, Flow flow) {
    Record record = flow.getRecord();

    if (recordCount == 0) {
      for (int i = 0; i < record.fieldCount(); ++i) {
        if (i > 0) {
          printStream.print(",");
        }
        Name name = record.getFieldName(i);
        printStream.print(name.getLocalName());
      }
      printStream.println();
    }

    for (int i = 0; i < record.fieldCount(); ++i) {
      if (i > 0) {
        printStream.print(",");
      }
      Value value = record.getValue(i);
      String s = value.getString();
      printStream.print(s);
    }
    printStream.println();
    ++recordCount;
  }

  public final void close() {
    ServingXmlException badDispose = null;
    try {
      printStream.flush();
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    try {
      sink.close();
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }
}


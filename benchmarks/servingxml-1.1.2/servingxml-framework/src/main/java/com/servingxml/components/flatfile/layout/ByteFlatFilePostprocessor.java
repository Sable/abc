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

package com.servingxml.components.flatfile.layout;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.flatfile.RecordOutput;

/**
 * A <code>ByteFlatFilePostprocessor</code> postproccesses a flat file.
 *
 * 
 * @author  Daniel A. Parker
 */

public class ByteFlatFilePostprocessor implements FlatFilePostprocessor {
  private final StreamSink sink;
  private final OutputStream outputStream;  
  private final boolean flushRecordOnWrite;

  public ByteFlatFilePostprocessor(StreamSink sink, boolean flushRecordOnWrite) {
    this.sink = sink;
    this.flushRecordOnWrite = flushRecordOnWrite;
    this.outputStream = new BufferedOutputStream(sink.getOutputStream());
  }

  public void write(RecordOutput recordOutput)
  throws IOException {
    byte[] data = recordOutput.toByteArray();
    outputStream.write(data, 0, data.length);
    if (flushRecordOnWrite) {
      outputStream.flush();
    }
  }

  public void beginData() {
  }

  public void endData() {
  }

  public void signFile(ServiceContext context, Flow flow, long recordCount) {
  }

  public void close() {
    ServingXmlException badDispose = null;
    try {
      if (outputStream != null) {
        outputStream.flush();
      }
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(), e);
    }
    try {
      if (sink != null) {
        sink.close();
      }
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(), e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }
}


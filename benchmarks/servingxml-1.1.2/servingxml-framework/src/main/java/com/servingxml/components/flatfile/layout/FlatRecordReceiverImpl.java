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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

/**
 * A <code>DelimitedFlatFileReader</code> implements a <code>RecordReader</code> interface.
 *
 * 
 * @author  Daniel A. Parker
 */

class FlatRecordReceiverImpl implements FlatRecordReceiver {
  private final ServiceContext context;
  private final Flow flow;
  private RecordReceiver recordReceiver;
  private final FlatRecordReader[] headerReaders;
  private final FlatRecordReader[] trailerReaders;
  private final FlatRecordReader flatRecordReader;
  private final RecordWriter recordWriter;
  private int lineNumber = 0;
  private int headerCount = 0;
  private int trailerCount = 0;

  public FlatRecordReceiverImpl(final ServiceContext context, final Flow flow,
    FlatRecordReader[] headerReaders, FlatRecordReader[] trailerReaders,
    FlatRecordReader flatRecordReader, final RecordWriter recordWriter) {
    this.context = context;
    this.flow = flow;
    this.headerReaders = headerReaders;
    this.trailerReaders = trailerReaders;
    this.flatRecordReader = flatRecordReader;
    this.recordWriter = recordWriter;
  }

  public void startFlatFile() {
    //recordWriter.startRecordStream(context, flow);

    this.recordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        //System.out.println(getClass().getName()+" "+record.toXmlString(context));
        Flow newFlow = flow.replaceRecord(context, record, lineNumber);
        recordWriter.writeRecord(context, newFlow);
      }
    };
  }
  public void endFlatFile() {
    //recordWriter.endRecordStream(context, flow);
    flatRecordReader.endReadRecords(context, flow, DelimiterExtractor.EMPTY_ARRAY, 0, 0, Integer.MAX_VALUE, recordReceiver);
  }
  public void headerRecord(RecordInput recordInput) {
    try {
      ++lineNumber;
      if (headerCount < headerReaders.length) {
        int index = headerReaders[headerCount].calculateFixedRecordLength(flow.getParameters(),flow.getRecord());
        recordInput.setPosition(index);
      }
      ++headerCount;
      //if (headerCount < headerReaders.length) {
      //  headerReaders[headerCount++].readRecord(context, parameters, data, start, length, 0, metaRecordReceiver);
      //}
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
  public void trailerRecord(RecordInput recordInput) {
    try {
      ++lineNumber;
      if (trailerCount < trailerReaders.length) {
        int index = trailerReaders[trailerCount].calculateFixedRecordLength(flow.getParameters(),flow.getRecord());
        recordInput.setPosition(index);
      }
      ++trailerCount;
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
    //if (trailerCount < trailerReaders.length) {
    //  trailerReaders[trailerCount++].readRecord(context, parameters, data, start, length, 0, metaRecordReceiver);
    //}
    //System.out.println("trailerRecord " + new String(data,start,length));
  }
  public void bodyRecord(RecordInput recordInput) {
    //long startMem = Runtime.getRuntime().totalMemory();
    //System.out.println(getClass().getName() + ".bodyRecord enter " + startMem);
    ++lineNumber;
    //System.out.println ("bodyRecord " + new String(data,start,length));
    //int consumed = 0;
    // 
    flatRecordReader.readRecord(context, flow, recordInput, DelimiterExtractor.EMPTY_ARRAY, 0, 0, 
                                Integer.MAX_VALUE, recordReceiver);
  }
}



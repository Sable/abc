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

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class FlatContentValidator implements FlatContentReceiver {
  private static final int INITIAL = 0;
  private static final int HEADER = 1;
  private static final int TRAILER = 2;
  private static final int BODY = 3;

  private final ServiceContext context;
  private final Flow flow;
  private final FlatFileSignature[] flatFileSignatures;
  private final FlatContentReceiver tail;
  private final FlatRecordReader[] headerReaders;
  private final FlatRecordReader[] trailerReaders;
  private RecordReceiver metaRecordReceiver;
  private int headerCount = 0;
  private int trailerCount = 0;
  private int level = 0;                     
  private int state = INITIAL;
  private RecordInput recordInput;

  public FlatContentValidator(final ServiceContext context, Flow flow,
    FlatRecordReader[] headerReaders, FlatRecordReader[] trailerReaders,
    final FlatFileSignature[] flatFileSignatures,
    FlatContentReceiver tail) {
    this.context = context;
    this.flow = flow;
    this.flatFileSignatures = flatFileSignatures;
    this.headerReaders = headerReaders;
    this.trailerReaders = trailerReaders;
    this.tail = tail;
  }

  public void startFlatFile() {
    metaRecordReceiver = new RecordReceiver() {
      public void receiveRecord(Record record) {
        for (int i = 0; i < flatFileSignatures.length; ++i) {
          flatFileSignatures[i].readMetaRecord(context, flow.getParameters(), record);
        }
      }
    };
    tail.startFlatFile();
  }

  public void endFlatFile() { 
    for (int i = 0; i < flatFileSignatures.length; ++i) {
      flatFileSignatures[i].validate(context, flow.getParameters());
    }
    tail.endFlatFile();
  }
  public void startHeader() { 
    state = HEADER;
    tail.startHeader();
  }
  public void endHeader() { 
    state = INITIAL;
    tail.endHeader();
  }
  public void startBody() { 
    state = BODY;
    tail.startBody();
  }
  public void endBody() { 
    state = INITIAL;
    tail.endBody();
  }
  public void startTrailer() { 
    state = TRAILER;
    tail.startTrailer();
  }
  public void endTrailer() { 
    state = INITIAL;
    tail.endTrailer();
  }
  public void startRecord() { 
    if (level >= 1) {
      throw new ServingXmlException("Parsing failed - unexpected start record");
    }
    this.recordInput = null;
    ++level;
    tail.startRecord();                              
  }
  public void endRecord() { 
    //System.out.println (getClass().getName()+"endRecord enter");

    switch (state) {
      case HEADER:
        if (headerCount < headerReaders.length) {
          headerReaders[headerCount++].readRecord(context, flow, recordInput, DelimiterExtractor.EMPTY_ARRAY, 0, 0, Integer.MAX_VALUE, metaRecordReceiver);
        }
        break;
      case BODY:
        break;
      case TRAILER:
        if (trailerCount < trailerReaders.length) {
          trailerReaders[trailerCount++].readRecord(context, flow, recordInput, DelimiterExtractor.EMPTY_ARRAY, 0, 0, Integer.MAX_VALUE, metaRecordReceiver);
        }
        break;
    }
    recordInput = null;
    --level;
    //System.out.println (getClass().getName()+"endRecord before tail.endRecord");
    tail.endRecord();

    //System.out.println (getClass().getName()+"endRecord leave");
  }

  public void commentLine(byte[] data, int start, int length) {
    if (state == BODY) {
      for (int i = 0; i < flatFileSignatures.length; ++i) {
        flatFileSignatures[i].data(data, start, length);
      }
    }
    tail.commentLine(data, start, length);
  }

  public void data(RecordInput ri) {
    //System.out.println (getClass().getName()+"data enter");

    if (state == BODY) {
      for (int i = 0; i < flatFileSignatures.length; ++i) {
        byte[] buffer = ri.toByteArray();
        flatFileSignatures[i].data(buffer, 0, buffer.length);
      }
    }

    if (state == HEADER || state == TRAILER) {
      if (level >= 1) {
        if (recordInput == null) {
          this.recordInput = ri;
        } else {
          recordInput = recordInput.concatenate(ri);
        }
      }

    }
    //System.out.println (getClass().getName()+"data before tail.data");
    tail.data(ri);
    //System.out.println (getClass().getName()+"data leave");
  }

  public void ignorableWhitespace(byte[] data, int start, int length) {
    if (state == BODY) {
      for (int i = 0; i < flatFileSignatures.length; ++i) {
        flatFileSignatures[i].data(data, start, length);
      }
    }
  }

  public void delimiter(byte[] data, int start, int length) { 
    if (state == BODY) {
      for (int i = 0; i < flatFileSignatures.length; ++i) {
        flatFileSignatures[i].data(data, start, length);
      }
    }
    tail.delimiter(data,start,length);
  }

  public void lineContinuation(byte[] data, int start, int length) {
    if (state == BODY) {
      for (int i = 0; i < flatFileSignatures.length; ++i) {
        flatFileSignatures[i].data(data, start, length);
      }
    }
    tail.lineContinuation(data,start,length);
  }
}





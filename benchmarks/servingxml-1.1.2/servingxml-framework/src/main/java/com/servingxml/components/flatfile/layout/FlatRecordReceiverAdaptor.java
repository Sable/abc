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
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.FlatRecordReceiver;

public class FlatRecordReceiverAdaptor implements FlatContentReceiver {
  private static final int INITIAL = 0;
  private static final int HEADER = 1;
  private static final int TRAILER = 2;        
  private static final int BODY = 3;

  private final FlatRecordReceiver receiver;
  private int level = 0;                     
  private int state = INITIAL;
  private RecordInput recordInput;

  public FlatRecordReceiverAdaptor(FlatRecordReceiver receiver) {
    this.receiver = receiver;
  }

  public void startFlatFile() {
    receiver.startFlatFile();
  }

  public void endFlatFile() {
    receiver.endFlatFile();
    if (level != 0) {
      throw new ServingXmlException("Parsing failed - unmatched record delimiter");
    }
  }

  public void startHeader() {
    state = HEADER;
  }

  public void endHeader() {
    state = INITIAL;
  }

  public void startBody() {
    state = BODY;
  }

  public void endBody() {
    state = INITIAL;
  }

  public void startTrailer() {
    state = TRAILER;
  }

  public void endTrailer() {
    state = INITIAL;
  }

  public void startRecord() {
    if (level >= 1) {
      throw new ServingXmlException("Parsing failed - unexpected start record");
    }
    recordInput = null;
    ++level;
  }

  public void endRecord() {
    //System.out.println (getClass().getName()+".endRecord enter");
    int length = 0;
    switch (state) {
      case HEADER:
        receiver.headerRecord(recordInput);
        break;
      case BODY:
        receiver.bodyRecord(recordInput);
        break;
      case TRAILER:
        receiver.trailerRecord(recordInput);
        break;
    }
    recordInput = null;
    --level;
  }

  public void commentLine(byte[] data, int start, int length) {
  }

  public void data(RecordInput ri) {
    if (level >= 1) {
      if (recordInput == null) {
        this.recordInput = ri;
      } else {
        this.recordInput = recordInput.concatenate(ri);
      }
    }
  }

  public void delimiter(byte[] data, int start, int length) {
  }

  public void lineContinuation(byte[] data, int start, int length) {
  }

  public void ignorableWhitespace(byte[] data, int start, int length) {
  }
}





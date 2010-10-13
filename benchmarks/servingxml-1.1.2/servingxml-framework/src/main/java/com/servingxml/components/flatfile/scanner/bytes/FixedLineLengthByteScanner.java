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

package com.servingxml.components.flatfile.scanner.bytes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;

public class FixedLineLengthByteScanner implements FlatFileScanner {
  private final int headerRecordCount;
  private final int trailerRecordCount;
  private final FlatRecordReader[] headerReaders;
  private final FlatRecordReader[] trailerReaders;
  private final Charset charset;

//  private BufferedInputStream is = null;
  private FlatContentReceiver receiver = null;
  private boolean eof = false;

  public FixedLineLengthByteScanner(FlatRecordReader[] headerReaders, 
                                    FlatRecordReader[] trailerReaders, 
                                    FlatFileOptions flatFileOptions) {
    this.headerRecordCount = headerReaders.length;
    this.trailerRecordCount = trailerReaders.length;
    this.eof = false;
    this.headerReaders = headerReaders;
    this.trailerReaders = trailerReaders;
    this.charset = flatFileOptions.getDefaultCharset();
  }

  public void scan(ServiceContext context, Flow flow, 
                   InputStream inputStream, FlatContentReceiver receiver) {

    int reserved = 0;
    for (int i = 0; i < trailerReaders.length; ++i) {
      reserved += trailerReaders[i].calculateFixedRecordLength(flow.getParameters(),flow.getRecord());
    }

    //this.is = inputStream;
    this.receiver = receiver;

    int lineNumber = 0;
    RecordBlock recordBuffer = new InputStreamRecordBlock(new BufferedInputStream(inputStream));
    recordBuffer.setReserved(reserved);
    RecordInput recordInput = new FixedLengthByteRecordInput(recordBuffer, charset);

    try {
      receiver.startFlatFile();
      if (headerRecordCount > 0) {
        receiver.startHeader();
        for (int i = 0; !recordInput.done() && i < headerRecordCount; ++i) {
          receiver.startRecord();
          receiver.data(recordInput);
          //System.out.println (getClass().getName()+".scan before endRecord 0");
          receiver.endRecord();
          recordInput.wipe();
          //System.out.println (getClass().getName()+".scan after endRecord 0");
          //System.out.println("Header bytes consumed = " + len);
        }
        receiver.endHeader();
      }
      int lookAheadCount = trailerRecordCount + 1;

      receiver.startBody();

      while (!recordInput.done()) {
        //System.out.println("begin: position="+recordInput.getPosition());
        int startPos = recordInput.getPosition();
        receiver.startRecord();
        receiver.data(recordInput);
        //System.out.println (getClass().getName()+".scan before endRecord 10");
        receiver.endRecord();
        int endPos = recordInput.getPosition();
        if (startPos == endPos) {
          //recordInput.setPosition(recordInput.getPosition()+1);
          //byte[] bytes = new byte[100];
          //while (!recordInput.done()) {
          //  recordInput.readBytes(bytes);
          //}
          throw new ServingXmlException("Fixed record reader consumes no data, record length is zero");
        }
        //System.out.println("end: position="+recordInput.getPosition());
        recordInput.wipe();
        //System.out.println (getClass().getName()+".scan after endRecord 10");
        //System.out.println("Before:  Actual record width = " + actualLen + ", position = " + recordBuffer.getPosition());
      }                                     
      receiver.endBody();
      recordBuffer.setReserved(0);

      if (trailerRecordCount > 0) {
        receiver.startTrailer();
        for (int i = 0; !recordInput.done() && i < trailerRecordCount; ++i) {
          receiver.startRecord();
          receiver.data(recordInput);
          //System.out.println (getClass().getName()+".scan before endRecord 100");
          receiver.endRecord();
          recordInput.wipe();
          //System.out.println (getClass().getName()+".scan after endRecord 100");
          //System.out.println("Trailer bytes consumed = " + actualLen);
        }
        receiver.endTrailer();
      }

      receiver.endFlatFile();
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
}


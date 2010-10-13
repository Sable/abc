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

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.flatfile.options.CommentStarter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.components.flatfile.scanner.bytes.LineDelimitedByteScanner;
import com.servingxml.components.flatfile.scanner.characters.LineDelimitedCharScanner;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.recordio.AbstractRecordReader;

/**
 * A <code>DelimitedFlatFileReader</code> implements a <code>RecordReader</code> interface.
 *
 * 
 * @author  Daniel A. Parker
 */

public class DefaultFlatFileReader extends AbstractRecordReader implements RecordReader {

  private final FlatFileOptions flatFileOptions;
  private final StreamSource source;

  public DefaultFlatFileReader(FlatFileOptions flatFileOptions, StreamSource source) {
    this.flatFileOptions = flatFileOptions;
    this.source = source;
  }

  public void readRecords(final ServiceContext context, final Flow flow) {
    //System.out.println(getClass().getName()+".readRecords ");

    InputStream is = null;
    boolean success = false;
    ServingXmlException badDispose = null;
    try {
      startRecordStream(context,flow);

      FlatFileScanner defaultScanner;

      if (flatFileOptions.isCountPositionsInBytes()) {
        defaultScanner = new LineDelimitedByteScanner(1, 0, flatFileOptions);
      } else {
        defaultScanner = new LineDelimitedCharScanner(1, 0, flatFileOptions);
      }

      FlatRecordReceiver recordReceiver = new DefaultFlatRecordReceiver(context, 
                                                                        flow, 
                                                                        flatFileOptions,
                                                                        getRecordWriter());

      FlatContentReceiver contentReceiver = new FlatRecordReceiverAdaptor(recordReceiver); 

      is = source.openStream();
      defaultScanner.scan(context, flow, is, contentReceiver); 
      endRecordStream(context,flow);
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(), e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
        }
      }
      try {
        close();
      } catch (Exception e) {
      }
      if (badDispose != null) {
        throw badDispose;
      }
    }
  }

  public Key getKey() {
    return source.getKey();
  }

  public Expirable getExpirable() {
    return source.getExpirable();
  }
}

class DefaultFlatRecordReceiver implements FlatRecordReceiver {
  private final int maxRecordWidth;
  private final Name recordTypeName;
  private final RecordWriter recordWriter;
  private final ServiceContext context;
  private final Flow initialFlow;
  private final FlatFileOptions flatFileOptions;
  private int lineNumber = 0;

  private Name[] fieldNames = new Name[0];

  public DefaultFlatRecordReceiver(ServiceContext context,
                                   Flow initialFlow,
                                   FlatFileOptions flatFileOptions,
                                   RecordWriter recordWriter) {

    this.flatFileOptions = flatFileOptions;
    this.maxRecordWidth = Integer.MAX_VALUE;
    this.context = context;
    this.initialFlow = initialFlow;
    this.recordWriter = recordWriter;

    this.recordTypeName = new QualifiedName("record");
  }

  public void bodyRecord(RecordInput recordInput) {
    //System.out.println(getClass().getName()+".bodyRecord " + fieldNames.length);
    try {
      ++lineNumber;
      RecordBuilder recordBuilder = new RecordBuilder(recordTypeName);

      for (int i = 0; i < fieldNames.length && !recordInput.done(); ++i) {
        String s = recordInput.readString(maxRecordWidth, flatFileOptions);
        recordBuilder.setString(fieldNames[i], s);
      }
      Record record = recordBuilder.toRecord();
      Flow flow = initialFlow.replaceRecord(context,record,lineNumber);
      recordWriter.writeRecord(context, flow);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void endFlatFile() {
  }

  public void headerRecord(RecordInput recordInput) {
    //System.out.println(getClass().getName()+".headerRecord " + new String(recordInput.buffer(),recordInput.start(),recordInput.length()));
    try {

      ++lineNumber;
      List<Name> fieldNameList = new ArrayList<Name>();
      boolean done = false;
      while (!done && !recordInput.done()) {
        String s = recordInput.readString(maxRecordWidth, flatFileOptions);
        String name = StringHelper.constructNameFromValue(s);
        if (name.length() == 0) {
          done = true;
        } else {
          Name fieldName = new QualifiedName(name);
          fieldNameList.add(fieldName);
        }
      }
      fieldNames = new Name[fieldNameList.size()];
      fieldNames = fieldNameList.toArray(fieldNames);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
  public void startFlatFile() {
  }
  public void trailerRecord(RecordInput recordInput) {
  }
}





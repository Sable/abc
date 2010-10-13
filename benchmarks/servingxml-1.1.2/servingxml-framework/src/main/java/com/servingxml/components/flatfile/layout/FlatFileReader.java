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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.recordio.AbstractRecordReader;

/**
 * A <code>DelimitedFlatFileReader</code> implements a <code>RecordReader</code> interface.
 *
 * 
 * @author  Daniel A. Parker
 */

public class FlatFileReader extends AbstractRecordReader implements RecordReader {

  private final FlatFileScanner flatFileScanner;
  private final FlatRecordReader[] headerReaders;
  private final FlatRecordReader[] trailerReaders;
  private final FlatRecordReader flatRecordReaderResolver;
  private final FlatFileSignature[] flatFileSignatures;
  private final StreamSource source;
  private final long fromRecord;
  private final long maxRecordCount;
  private final boolean lineDelimited;

  public FlatFileReader(FlatFileScanner flatFileScanner, 
    FlatRecordReader[] headerReaders, FlatRecordReader[] trailerReaders,
    FlatRecordReader flatRecordReaderResolver,
    FlatFileSignature[] flatFileSignatures, StreamSource source, long fromRecord, long maxRecordCount,
    boolean lineDelimited) {
    this.flatFileScanner = flatFileScanner;
    this.headerReaders = headerReaders;
    this.trailerReaders = trailerReaders;
    this.flatRecordReaderResolver = flatRecordReaderResolver;
    this.flatFileSignatures = flatFileSignatures;
    this.source = source;
    this.fromRecord = fromRecord;
    this.maxRecordCount = maxRecordCount;
    this.lineDelimited = lineDelimited;
  }

  public void readRecords(final ServiceContext context, final Flow flow) {

    InputStream is = null;
    boolean success = false;

    try {
      startRecordStream(context,flow);
      FlatRecordReceiver recordReceiver = new FlatRecordReceiverImpl(context, flow,
                                            headerReaders, trailerReaders, flatRecordReaderResolver, 
        getRecordWriter());
      FlatContentReceiver contentReceiver;
      if (lineDelimited) {
        contentReceiver = new FlatRecordReceiverAdaptor(recordReceiver);
      } else {
        contentReceiver = new NonDelimitedFlatContentReceiver(recordReceiver);
      }

      if (flatFileSignatures.length > 0) {
        contentReceiver = new FlatContentValidator(context, flow,
                            headerReaders,trailerReaders,flatFileSignatures,contentReceiver);
      }
      is = source.openStream();
      flatFileScanner.scan(context, flow, is, contentReceiver);
      endRecordStream(context,flow);
      success = true;
    } finally {
      try {
        close();
      } catch (Exception e) {
        //  Don't care
      }
      try {
        if (is != null) {
          source.closeStream(is);
        }
      } catch (IOException e) {
        if (success) {
          throw new ServingXmlException(e.getMessage(),e);
        }
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


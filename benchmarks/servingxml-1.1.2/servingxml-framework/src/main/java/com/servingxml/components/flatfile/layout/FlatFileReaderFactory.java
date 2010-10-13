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

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.AbstractRecordReaderFactory;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.app.Flow;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;

/**
 * A <code>FlatFileReaderFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordReader</code> interface.
 *
 *                              
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileReaderFactory extends AbstractRecordReaderFactory 
implements RecordReaderFactory, RecordFilterAppender {     

  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final FlatFile flatFile;
  private final StreamSourceFactory streamSourceFactory;
  private final long fromRecord;
  private final long maxRecordCount;

  public FlatFileReaderFactory(FlatFileOptionsFactory flatFileOptionsFactory,
    StreamSourceFactory streamSourceFactory, FlatFile flatFile,
    long fromRecord, long maxRecordCount) {
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.flatFile = flatFile; 
    this.streamSourceFactory = streamSourceFactory;
    this.fromRecord = fromRecord;
    this.maxRecordCount = maxRecordCount;
  }
  
  protected RecordReader createRecordReader(ServiceContext context, Flow flow) {

    StreamSource source = streamSourceFactory.createStreamSource(context, flow);
    //System.out.println(getClass().getName()+".createRecordReader isBinary="+flatFile.isBinary());
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, 
                                                                                   flatFile.isFieldDelimited(), 
                                                                                   !flatFile.isText() || flatFile.isSignatures(), 
                                                                                   source.getCharset());
    RecordReader recordReader = flatFile.createRecordReader(context, flow, source, fromRecord, maxRecordCount, flatFileOptions);
    return recordReader;
  }
}

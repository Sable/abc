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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordWriterFactory;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.RecordWriterFactory;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;

/**
 * A <code>FlatFileWriterFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordWriter</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileWriterFactory extends AbstractRecordWriterFactory
implements RecordWriterFactory {     

  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final FlatFile flatFile;
  private final StreamSinkFactory sinkFactory;

  public FlatFileWriterFactory(FlatFileOptionsFactory flatFileOptionsFactory,
    FlatFile flatFile, StreamSinkFactory sinkFactory) {
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.flatFile = flatFile; 
    this.sinkFactory = sinkFactory;
  }

  public RecordWriter createRecordWriter(ServiceContext context, Flow flow) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, 
                                                                                   flatFile.isFieldDelimited(), 
                                                                                   !flatFile.isText() || flatFile.isSignatures(),
                                                                                   sinkFactory.getCharset());
    RecordWriter recordWriter = flatFile.createRecordWriter(context, flow, sinkFactory, flatFileOptions);
    return recordWriter;
  }
}

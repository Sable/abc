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
import com.servingxml.components.recordio.AbstractRecordWriterFactory;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.RecordWriterFactory;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.FieldDelimiter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;

/**
 * Implements a default flat file writer.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DefaultFlatFileWriterFactory extends AbstractRecordWriterFactory
implements RecordWriterFactory {     

  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final StreamSinkFactory sinkFactory;

  public DefaultFlatFileWriterFactory(FlatFileOptionsFactory flatFileOptionsFactory, StreamSinkFactory sinkFactory) {
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.sinkFactory = sinkFactory;
  }

  public RecordWriter createRecordWriter(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".createRecordWriter enter");
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, true, false, sinkFactory.getCharset());
    Delimiter fieldDelimiter;
    if (flatFileOptions.getFieldDelimiters().length == 0) {
      fieldDelimiter = FieldDelimiter.COMMA;
    } else {
      fieldDelimiter = flatFileOptions.getFieldDelimiters()[0];
    }
    Delimiter recordDelimiter = flatFileOptions.getRecordDelimiterForWriting();
    QuoteSymbol quoteSymbol = flatFileOptions.getQuoteSymbol();
    RecordWriter recordWriter = new DefaultFlatFileWriter(sinkFactory, fieldDelimiter, recordDelimiter,
      quoteSymbol);
    return recordWriter;
  }
}

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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.AbstractRecordWriter;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.record.FieldType;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.components.flatfile.options.Delimiter;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class DefaultFlatFileWriter extends AbstractRecordWriter implements RecordWriter {
  private final StreamSinkFactory sinkFactory;
  private final Delimiter fieldDelimiter;
  private final Delimiter recordDelimiter;
  private final QuoteSymbol quoteSymbol;
  private StreamSink sink = null;
  private int row;
  private Writer writer = null;
  private final StringBuilder buf;

  public DefaultFlatFileWriter(StreamSinkFactory sinkFactory, Delimiter fieldDelimiter, 
    Delimiter recordDelimiter, QuoteSymbol quoteSymbol) {
    this.sinkFactory = sinkFactory;
    this.fieldDelimiter = fieldDelimiter;
    this.recordDelimiter = recordDelimiter;
    this.quoteSymbol = quoteSymbol;
    this.buf = new StringBuilder();
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".startRecordStream enter");
    try {

      this.sink = sinkFactory.createStreamSink(context, flow);
      if (sink.getCharset() != null) {
        this.writer = new BufferedWriter(new OutputStreamWriter(sink.getOutputStream(), sink.getCharset()));
      } else {
        this.writer = new BufferedWriter(new OutputStreamWriter(sink.getOutputStream()));
      }
      row = 0;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    //System.out.println(getClass().getName()+".startRecordStream leave");
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".writeRecord enter");
    try {

      Record record = flow.getRecord();
      int fieldCount = record.fieldCount();
      if (row == 0) {
        for (int i = 0; i < fieldCount; ++i) {
          if (i > 0) {
            writer.write(fieldDelimiter.toString());
          }
          FieldType fieldType = record.getRecordType().getFieldType(i);
          String value = fieldType.getLabel();
          boolean insertQuotes = fieldDelimiter.occursIn(value) || recordDelimiter.occursIn(value);
          if (insertQuotes) {
            buf.setLength(0);
            buf.append(quoteSymbol.getCharacter());
            quoteSymbol.escape(value, buf);
            buf.append(quoteSymbol.getCharacter());
            writer.write(buf.toString());
          } else {
            writer.write(value);
          }
        }
        writer.write(recordDelimiter.toString());
      }
      for (int i = 0; i < fieldCount; ++i) {
        if (i > 0) {
          writer.write(fieldDelimiter.toString());
        }
        Name fieldName = record.getFieldName(i);
        String value = record.getValue(i).getString();
        boolean insertQuotes = fieldDelimiter.occursIn(value) || recordDelimiter.occursIn(value);
        if (insertQuotes) {
          buf.setLength(0);
          buf.append(quoteSymbol.getCharacter());
          quoteSymbol.escape(value, buf);
          buf.append(quoteSymbol.getCharacter());
          writer.write(buf.toString());
        } else {
          writer.write(value);
        }
      }
      writer.write(recordDelimiter.toString());
      ++row;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    //System.out.println(getClass().getName()+".writeRecord leave");
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".endRecordStream enter");
    try {
      writer.flush();
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    //System.out.println(getClass().getName()+".endRecordStream leave");
  }

  public void close() {
    if (sink != null) {
      sink.close();
      sink = null;
    }
  }
}                                          



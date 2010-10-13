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

package com.servingxml.components.flatfile.recordtype;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.Formatter;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.RecordOutput;

public class MultivaluedDelimitedFieldWriter implements FlatRecordFieldWriter {
  private final Name fieldName;
  private final int start;
  private final Formatter fieldFormatter;
  private final DefaultValue defaultValueEvaluator;
  private final FlatFileOptions flatFileOptions;
  private final Delimiter fieldDelimiter;
  private final Delimiter subfieldDelimiter;
  private final StringBuilder buf;

  public MultivaluedDelimitedFieldWriter(Name fieldName, int start, Formatter fieldFormatter, 
    DefaultValue defaultValueEvaluator, FlatFileOptions flatFileOptions) {
    this.fieldName = fieldName;
    this.start = start;
    this.fieldFormatter = fieldFormatter; 
    this.defaultValueEvaluator = defaultValueEvaluator;
    this.flatFileOptions = flatFileOptions;
    this.buf = new StringBuilder();
    Delimiter[] fieldDelimiters = flatFileOptions.getFieldDelimiters();
    this.fieldDelimiter = fieldDelimiters.length == 0 ? Delimiter.NULL : fieldDelimiters[0];
    Delimiter[] subfieldDelimiters = flatFileOptions.getSubfieldDelimiters();
    this.subfieldDelimiter = subfieldDelimiters.length == 0 ? Delimiter.NULL : subfieldDelimiters[0];
  }

  public void writeField(ServiceContext context, Flow flow, RecordOutput recordOutput) {
    writeField(context, flow, fieldName, recordOutput);
  }

  public void writeField(ServiceContext context, Flow flow, Name fieldName, RecordOutput recordOutput) {
    int offset = flatFileOptions.rebaseIndex(start);
    if (offset >= 0) {
      recordOutput.setPosition(offset);
    }

    buf.setLength(0);
    Record record = flow.getRecord();

    String[] values = record.getStringArray(fieldName);
    if (values == null) {
      values = defaultValueEvaluator.evaluateStringArray(context, flow);
    }
    final QuoteSymbol quoteSymbol = flatFileOptions.getQuoteSymbol();
    for (int i = 0; i < values.length; ++i) {
      String value = values[i];
      value = fieldFormatter.format(value);
      if (i > 0) {
        subfieldDelimiter.writeEndDelimiterTo(buf);
      }
      boolean insertQuotes = flatFileOptions.useQuotes(value);
      if (insertQuotes) {
        buf.append(quoteSymbol.getCharacter());
        quoteSymbol.escape(value, buf);
        buf.append(quoteSymbol.getCharacter());
      } else {
        buf.append(value);
      }
    }
    recordOutput.writeString(buf.toString());
  }

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
    fieldDelimiter.writeEndDelimiterTo(recordOutput);
  }
}

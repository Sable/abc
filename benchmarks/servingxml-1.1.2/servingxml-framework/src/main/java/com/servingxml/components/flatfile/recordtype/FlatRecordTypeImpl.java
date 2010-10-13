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
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.app.Flow;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;

/**
 * Class for flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypeImpl implements FlatRecordType {

  private final FlatRecordField[] flatRecordFields;
  private final NameSubstitutionExpr recordTypeNameExpr;
  private final FlatFileOptions flatFileOptions;
  private final IntegerSubstitutionExpr recordLengthExpr;

  public FlatRecordTypeImpl(NameSubstitutionExpr recordTypeNameExpr, 
                            FlatRecordField[] flatRecordFields, 
                            IntegerSubstitutionExpr recordLengthExpr,
                            FlatFileOptions flatFileOptions) {

    this.flatRecordFields = flatRecordFields;
    this.recordTypeNameExpr = recordTypeNameExpr;
    this.flatFileOptions = flatFileOptions;
    this.recordLengthExpr = recordLengthExpr;
    //System.out.println(getClass().getName()+".cons omitFinalFieldDelimiter=" + flatFileOptions.isOmitFinalFieldDelimiter());
    //System.out.println(getClass().getName()+".cons omitFinalRepeatDelimiter=" + flatFileOptions.isOmitFinalRepeatDelimiter());
  }

  public Record getDefaultRecord(ServiceContext context, Flow flow) {
    Name recordTypeName = recordTypeNameExpr.evaluateName(flow.getParameters(),flow.getRecord());
    RecordBuilder recordBuilder = new RecordBuilder(recordTypeName);
    for (int i = 0; i < flatRecordFields.length; ++i) {
      FlatRecordField field = flatRecordFields[i];
      recordBuilder.setString(field.getName(), field.getLabel(context, flow));
    }
    return recordBuilder.toRecord();
  }

  public FlatRecordReader createFlatRecordReader() {
    FlatRecordFieldReader[] flatRecordFieldReaders = new FlatRecordFieldReader[flatRecordFields.length];
    for (int i = 0; i < flatRecordFields.length; ++i) {
      flatRecordFieldReaders[i] = flatRecordFields[i].createFlatRecordFieldReader();
    }
    FlatRecordReader flatRecordReader = new FlatRecordReaderImpl(recordTypeNameExpr, flatRecordFieldReaders,
                                                                 recordLengthExpr);
    return flatRecordReader;
  }

  public FlatRecordWriter createFlatRecordWriter() {
    FlatRecordFieldWriter[] flatRecordFieldWriters = new FlatRecordFieldWriter[flatRecordFields.length];
    for (int i = 0; i < flatRecordFields.length; ++i) {
      flatRecordFieldWriters[i] = flatRecordFields[i].createFlatRecordWriter();
    }
    FlatRecordWriter flatRecordWriter = new FlatRecordWriterImpl(flatFileOptions.isOmitFinalRepeatDelimiter(),
                                                                 flatRecordFieldWriters);
    return flatRecordWriter;
  }

  public boolean isText() {
    boolean result = true;

    for (int i = 0; result && i < flatRecordFields.length; ++i) {
      if (!flatRecordFields[i].isText()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isBinary() {
    boolean result = true;

    for (int i = 0; result && i < flatRecordFields.length; ++i) {
      if (!flatRecordFields[i].isBinary()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isFixedLength() {
    return !recordLengthExpr.isNull();
  }
}

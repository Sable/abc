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
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;

/**
 * Class for flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MergeSegments implements FlatRecordType {

  private final FlatRecordType sdwlFlatRecordType;
  private final boolean suppressSDW;
  private final FlatRecordType dataFlatRecordType;
  private final GroupRecognizer groupRecognizer;
  private final IntegerSubstitutionExpr segmentLengthExpr;

  public MergeSegments(GroupRecognizer groupRecognizer,
                              FlatRecordType sdwlFlatRecordType,
                              boolean suppressSDW,
                              FlatRecordType dataFlatRecordType,
                              IntegerSubstitutionExpr segmentLengthExpr) {

    this.sdwlFlatRecordType = sdwlFlatRecordType;
    this.suppressSDW = suppressSDW;
    this.dataFlatRecordType = dataFlatRecordType;
    this.groupRecognizer = groupRecognizer;
    this.segmentLengthExpr = segmentLengthExpr;
  }

  public Record getDefaultRecord(ServiceContext context, Flow flow) {
    return dataFlatRecordType.getDefaultRecord(context,flow);
  }

  public FlatRecordReader createFlatRecordReader() {

    FlatRecordReader sdwRecordReader = sdwlFlatRecordType.createFlatRecordReader();
    FlatRecordReader dataRecordReader = dataFlatRecordType.createFlatRecordReader();
    FlatRecordReader flatRecordReader = new MergeSegmentsReader(groupRecognizer,
                                                                       sdwRecordReader,
                                                                       suppressSDW,
                                                                       dataRecordReader,
                                                                       segmentLengthExpr);
    return flatRecordReader;
  }

  public FlatRecordWriter createFlatRecordWriter() {
    FlatRecordWriter flatRecordWriter = dataFlatRecordType.createFlatRecordWriter();
    return flatRecordWriter;
  }

  public boolean isText() {
    return dataFlatRecordType.isText();
  }

  public boolean isBinary() {
    return dataFlatRecordType.isBinary();
  }

  public boolean isFixedLength() {
    return !segmentLengthExpr.isNull();
  }
}

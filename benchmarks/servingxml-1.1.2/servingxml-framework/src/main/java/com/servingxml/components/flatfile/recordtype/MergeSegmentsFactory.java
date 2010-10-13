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

import java.util.List;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SystemConstants;

/**
 * Class for flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MergeSegmentsFactory implements RecordCombinationFactory {

  private final FlatRecordTypeFactory dataRecordTypeFactory;
  private final GroupRecognizer groupRecognizer;
  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final IntegerSubstitutionExpr segmentLengthExpr;
  private final boolean suppressSDW;

  public MergeSegmentsFactory(GroupRecognizer groupRecognizer,
                                     FlatRecordTypeFactory dataRecordTypeFactory,
                                     IntegerSubstitutionExpr segmentLengthExpr,
                                     boolean suppressSDW,
                                     FlatFileOptionsFactory flatFileOptionsFactory) {

    this.groupRecognizer = groupRecognizer;
    this.dataRecordTypeFactory = dataRecordTypeFactory;
    this.segmentLengthExpr = segmentLengthExpr;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.suppressSDW = suppressSDW;
  }

  public FlatRecordType createFlatRecordType(ServiceContext context, Flow flow,
                                             FlatRecordType sdwRecordType, 
                                             FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);
    FlatRecordType dataRecordType = dataRecordTypeFactory.createFlatRecordType(context,flow,flatFileOptions);

    return new MergeSegments(groupRecognizer, sdwRecordType, suppressSDW, dataRecordType, segmentLengthExpr);
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
                                    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
  }

  public boolean isFieldDelimited() {
    return dataRecordTypeFactory.isFieldDelimited();
  }

  public boolean isBinary() {
    return dataRecordTypeFactory.isBinary();
  }

  public boolean isText() {
    return dataRecordTypeFactory.isText();
  }
}

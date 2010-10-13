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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SystemConstants;
import java.util.List;
import com.servingxml.components.common.NameSubstitutionExpr;

/**
 * Class for flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CombineFlatRecordsFactory implements RecordCombinationFactory {

  private final NameSubstitutionExpr recordTypeNameExpr;
  private final Name repeatingGroupFieldName;
  private final FlatRecordTypeFactory dataRecordTypeFactory;
  private final GroupRecognizer groupRecognizer;
  private final FlatFileOptionsFactory flatFileOptionsFactory;

  public CombineFlatRecordsFactory(NameSubstitutionExpr recordTypeNameExpr,
                                        Name repeatingGroupFieldName,
                                  GroupRecognizer groupRecognizer,
                                     FlatRecordTypeFactory dataRecordTypeFactory,
                                     FlatFileOptionsFactory flatFileOptionsFactory) {

    this.recordTypeNameExpr = recordTypeNameExpr;
    this.repeatingGroupFieldName = repeatingGroupFieldName;
    this.groupRecognizer = groupRecognizer;
    this.dataRecordTypeFactory = dataRecordTypeFactory;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }

  public FlatRecordType createFlatRecordType(ServiceContext context, Flow flow,
                                             FlatRecordType sdwRecordType, 
                                             FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);
    FlatRecordType dataRecordType = dataRecordTypeFactory.createFlatRecordType(context,flow,flatFileOptions);

    return new CombineFlatRecords(recordTypeNameExpr, repeatingGroupFieldName, groupRecognizer, sdwRecordType, dataRecordType);
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

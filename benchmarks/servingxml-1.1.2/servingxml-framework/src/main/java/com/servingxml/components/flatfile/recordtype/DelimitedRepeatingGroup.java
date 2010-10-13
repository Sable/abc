/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
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
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;

public class DelimitedRepeatingGroup implements FlatRecordField {
  private final Name fieldName;
  private final FlatRecordType segmentType;
  private final FlatFileOptions flatFileOptions;
  private final IntegerSubstitutionExpr startExpr;
  private final IntegerSubstitutionExpr countExpr;

  public DelimitedRepeatingGroup(Name fieldName, IntegerSubstitutionExpr startExpr, FlatRecordType segmentType, 
                                 FlatFileOptions flatFileOptions, IntegerSubstitutionExpr countExpr) {
    this.fieldName = fieldName;
    this.startExpr = startExpr;
    this.segmentType = segmentType;
    this.flatFileOptions = flatFileOptions;
    this.countExpr = countExpr;
  }

  public FlatRecordFieldReader createFlatRecordFieldReader() {
    FlatRecordReader segmentReader = segmentType.createFlatRecordReader();
    FlatRecordFieldReader reader = new DelimitedRepeatingGroupReader(fieldName, startExpr,
                                                                     segmentReader,  
                                                                     countExpr,
                                                                     flatFileOptions);
    return reader;
  }

  public FlatRecordFieldWriter createFlatRecordWriter() {
    FlatRecordWriter segmentWriter = segmentType.createFlatRecordWriter();

    FlatRecordFieldWriter writer = new DelimitedRepeatingGroupWriter(fieldName, startExpr, 
                                                                     segmentWriter, flatFileOptions);
    return writer;
  }

  public String getLabel(ServiceContext context, Flow flow) {
    return "";          
  }

  public Name getName() {
    return fieldName;
  }

  public boolean isText() {
    return segmentType.isText();
  }

  public boolean isBinary() {
    return segmentType.isBinary();
  }
}

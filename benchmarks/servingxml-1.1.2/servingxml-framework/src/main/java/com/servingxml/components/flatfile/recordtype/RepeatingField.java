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
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.flatfile.options.FlatFileOptions;

public class RepeatingField implements FlatRecordField {

  private final FlatRecordField namedField;
  private final IntegerSubstitutionExpr countExpr;
  private final FlatFileOptions flatFileOptions;

  public RepeatingField(FlatRecordField namedField, IntegerSubstitutionExpr countExpr, 
                              FlatFileOptions flatFileOptions) {

    this.namedField = namedField;
    this.countExpr = countExpr;
    this.flatFileOptions = flatFileOptions;
  }

  public FlatRecordFieldReader createFlatRecordFieldReader() {
    //System.out.println(getClass().getName()+"createFlatRecordFieldReader.Name enter");
    FlatRecordFieldReader namedFieldReader = namedField.createFlatRecordFieldReader();

    FlatRecordFieldReader nameValueReader = new RepeatingFieldReader(namedFieldReader, 
                                                                           countExpr, 
                                                                           flatFileOptions);
    return nameValueReader;

  }

  public FlatRecordFieldWriter createFlatRecordWriter() {
    FlatRecordFieldWriter writer = namedField.createFlatRecordWriter();
    return writer;
  }

  public Name getName() {
    return Name.EMPTY;          
  }

  public String getLabel(ServiceContext context, Flow flow) {
    return namedField.getLabel(context, flow);          
  }

  public boolean isText() {
    return namedField.isText();
  }

  public boolean isBinary() {
    return namedField.isBinary();
  }
}


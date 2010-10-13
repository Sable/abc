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

import java.io.IOException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class PositionalFieldReader implements FlatRecordFieldReader {
  private final Name name;
  private final int start;
  private final IntegerSubstitutionExpr fieldWidthExpr;
  private final boolean trimLeading;
  private final boolean trimTrailing;
  private final DefaultValue defaultValue;
  private final FlatFileOptions flatFileOptions;

  public PositionalFieldReader(Name name, int start, IntegerSubstitutionExpr fieldWidthExpr, 
                               DefaultValue defaultValue, 
                               FlatFileOptions flatFileOptions) {
    this.name = name;
    this.start = start;
    this.fieldWidthExpr = fieldWidthExpr;
    this.trimLeading = flatFileOptions.isTrimLeading();
    this.trimTrailing = flatFileOptions.isTrimTrailing();
    this.defaultValue = defaultValue;
    this.flatFileOptions = flatFileOptions;
  }

  public void readField(ServiceContext context, 
                        Flow flow, 
                        final RecordInput recordInput, 
                        DelimiterExtractor[] recordDelimiters, 
                        int recordDelimiterStart, 
                        int recordDelimiterCount, 
                        int maxRecordWidth,
                        RecordBuilder recordBuilder) {

    try {
      //System.out.println(getClass().getName()+".readField start="+start+",initial position="+recordInput.getPosition());
      int offset = flatFileOptions.rebaseIndex(start);
      if (offset >= 0) {
        recordInput.setPosition(offset);
      }
      //System.out.println(getClass().getName()+".readField final position="+recordInput.getPosition());

      int fieldWidth = fieldWidthExpr.evaluateAsInt(flow.getParameters(),recordBuilder);
      if (fieldWidth > 0) {
        //System.out.println(getClass().getName()+".readField *** position=" + recordInput.getPosition() 
        //   + ", last= " + recordInput.getLast()  + ", fieldWidth=" + fieldWidth);
        String value = recordInput.readString(fieldWidth);
        value = StringHelper.trim(value, trimLeading, trimTrailing); 
        //System.out.println(getClass().getName()+".readField name="+name+",length="+value.length()+", value="+value);
        if (value.length() == 0) {
          value = defaultValue.evaluateString(context,flow);
          //System.out.println(getClass().getName()+".readField defaultValue="+value);
        }
        recordBuilder.setString(name,value);
        //System.out.println(getClass().getName()+".readField position=" + recordInput.getPosition() 
        //  + ", last= " + recordInput.getLast() + ", length = "
        //  + recordInput.length());
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    int fieldWidth = fieldWidthExpr.evaluateAsInt(parameters, currentRecord);
    int offset = flatFileOptions.rebaseIndex(start);
    return offset >= 0 ? offset+fieldWidth : currentPosition+fieldWidth;
  }
}

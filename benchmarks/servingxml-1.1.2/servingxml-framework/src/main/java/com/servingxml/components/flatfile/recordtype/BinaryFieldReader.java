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
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class BinaryFieldReader implements FlatRecordFieldReader {
  private final Name name;
  private final int start;
  private final IntegerSubstitutionExpr fieldWidthExpr;
  private final FlatFileOptions flatFileOptions;

  public BinaryFieldReader(Name name, int start, IntegerSubstitutionExpr fieldWidthExpr, 
                           DefaultValue defaultValue, 
                           FlatFileOptions flatFileOptions) {
    this.name = name;
    this.start = start;
    this.fieldWidthExpr = fieldWidthExpr;
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

    //System.out.println(getClass().getName()+".readField " + name);
    try {
      int offset = flatFileOptions.rebaseIndex(start);
      if (offset >= 0) {
        recordInput.setPosition(offset);
      }

      int fieldWidth = fieldWidthExpr.evaluateAsInt(flow.getParameters(),recordBuilder);
      if (fieldWidth > 0) {
        //System.out.println(getClass().getName()+".readField *** position=" + recordInput.getPosition() 
        //   + ", last= " + recordInput.getLast()  + ", fieldWidth=" + fieldWidth);

        byte[] value = new byte[fieldWidth];
        int len = recordInput.readBytes(value);
        if (len == fieldWidth) {
          recordBuilder.setHexBinary(name,value);
        } else {
          byte[] v = new byte[len];
          System.arraycopy(value,0,v,0,len);
          recordBuilder.setHexBinary(name,v);
        }
        //System.out.println(getClass().getName()+"." + recordInput.getPosition() + " " + index+len);
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage());
    }
  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    int fieldWidth = fieldWidthExpr.evaluateAsInt(parameters, currentRecord); 
    int offset = flatFileOptions.rebaseIndex(start);
    return offset >= 0 ? offset+fieldWidth : currentPosition+fieldWidth;
  }
}

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
import java.util.HashMap;
import java.util.Map;

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

public class OverpunchFieldReader implements FlatRecordFieldReader {
//  Based on code contributed by Matt Dowell
// see #convertFromOverPunch(String) 

  private static Character[][] OVERPUNCH_VALUE_SET = { { '{', '}'}, 
    { 'A', 'J'}, { 'B', 'K'}, { 'C', 'L'}, { 'D', 'M'}, 
    { 'E', 'N'}, { 'F', 'O'}, { 'G', 'P'}, { 'H', 'Q'}, 
    { 'I', 'R'}}; 

  private static final Map<Character,Character> positiveEncoded = new HashMap<Character,Character>(); 
  private static final Map<Character,Character> negativeEncoded = new HashMap<Character,Character>(); 

  static { 
    for (int x = 0; x < OVERPUNCH_VALUE_SET.length; x++) {
      positiveEncoded.put(OVERPUNCH_VALUE_SET[x][0], Character.forDigit(x,10)); 
      negativeEncoded.put(OVERPUNCH_VALUE_SET[x][1], Character.forDigit(x,10)); 
    }  
  } 

  private final Name name;
  private final int start;
  private final IntegerSubstitutionExpr fieldWidthExpr;
  private final int decimalPlaces;
  private final boolean trimLeading;
  private final boolean trimTrailing;
  private final DefaultValue defaultValue;
  private final FlatFileOptions flatFileOptions;

  public OverpunchFieldReader(Name name, int start, IntegerSubstitutionExpr fieldWidthExpr, 
                              int decimalPlaces, DefaultValue defaultValue, 
                              FlatFileOptions flatFileOptions) {
    this.name = name;
    this.start = start;
    this.fieldWidthExpr = fieldWidthExpr;
    this.decimalPlaces = decimalPlaces;
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
        String overpunchValue = recordInput.readString(fieldWidth).trim();
        //System.out.println("overpunchValue="+overpunchValue);
        String value;
        if (overpunchValue.length() == 0) {
          value = defaultValue.evaluateString(context,flow);
          //System.out.println(getClass().getName()+".readField defaultValue="+overpunchValue);
          if (value.length() == 0) {
            value = "0";
          }
        } else {
          char overpunch = overpunchValue.charAt(overpunchValue.length()-1); 
          //System.out.println("overpunch="+overpunch);
          int indentity = 0; 
          char leastSig = 'X'; 
          if (positiveEncoded.containsKey(overpunch)) {
            indentity = 1; 
            leastSig = positiveEncoded.get(overpunch); 
            //System.out.println("positive encoded");
          } else if (negativeEncoded.containsKey(overpunch)) {
            indentity = -1; 
            leastSig = negativeEncoded.get(overpunch); 
            //System.out.println("negative encoded");
          } else if (Character.isDigit(overpunch)) {
            indentity = 1; 
            leastSig = overpunch; 
            //System.out.println("is digit");
          } else {
            indentity = 0;   
            leastSig = 'X'; 
          }

          StringBuilder rtn = new StringBuilder(overpunchValue.substring(0, overpunchValue.length() - 1)); 
          rtn.append(leastSig); 
          if (decimalPlaces > 0) {
            if (rtn.length() <= decimalPlaces) {
              for (int i = rtn.length(); i < decimalPlaces; ++i) {
                rtn.insert(0,'0');
              }
              rtn.insert(0,"0.");
            } else { 
              rtn.insert(rtn.length()-decimalPlaces,'.');
            }
          }
          if (indentity < 0) {
            rtn.insert(0, '-'); 
          }
          value = rtn.toString(); 
        }

        //System.out.println(getClass().getName()+".readField name="+name+",length="+overpunchValue.length()+", overpunchValue="+overpunchValue);
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

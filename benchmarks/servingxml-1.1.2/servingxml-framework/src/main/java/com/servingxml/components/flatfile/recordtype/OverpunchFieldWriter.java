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

import java.util.HashMap;
import java.util.Map;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.Alignment;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.Formatter;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.LineFormatter;

public class OverpunchFieldWriter implements FlatRecordFieldWriter {

  private static Character[][] OVERPUNCH_VALUE_SET = { { '{', '}'}, 
    { 'A', 'J'}, { 'B', 'K'}, { 'C', 'L'}, { 'D', 'M'}, 
    { 'E', 'N'}, { 'F', 'O'}, { 'G', 'P'}, { 'H', 'Q'}, 
    { 'I', 'R'}}; 

  private static final Map<Character,Character> positiveEncoded = new HashMap<Character,Character>(); 
  private static final Map<Character,Character> negativeEncoded = new HashMap<Character,Character>(); 

  static { 
    for (int x = 0; x < OVERPUNCH_VALUE_SET.length; x++) {
      positiveEncoded.put(Character.forDigit(x,10), OVERPUNCH_VALUE_SET[x][0]); 
      negativeEncoded.put(Character.forDigit(x,10), OVERPUNCH_VALUE_SET[x][1]); 
    }  
  } 
  private final Name fieldName;
  private final int start;
  private final IntegerSubstitutionExpr fieldWidthExpr;
  private final int decimalPlaces;
  private final DefaultValue defaultValueEvaluator;
  private final char padCharacter;
  private final Alignment alignment;
  private final FlatFileOptions flatFileOptions;

  public OverpunchFieldWriter(Name fieldName, int start, IntegerSubstitutionExpr fieldWidthExpr, 
    int decimalPlaces, DefaultValue defaultValueEvaluator, FlatFileOptions flatFileOptions) {
    this.fieldName = fieldName;
    this.start = start;
    this.fieldWidthExpr = fieldWidthExpr;
    this.decimalPlaces = decimalPlaces;
    this.defaultValueEvaluator = defaultValueEvaluator;
    this.padCharacter = flatFileOptions.getPadCharacter();
    this.alignment = Alignment.RIGHT;
    this.flatFileOptions = flatFileOptions;
  }

  public void writeField(ServiceContext context, Flow flow, RecordOutput recordOutput) {
    writeField(context, flow, fieldName, recordOutput);
  }

  public void writeField(ServiceContext context, Flow flow,
    Name fieldName, RecordOutput recordOutput) {

    int fieldWidth = fieldWidthExpr.evaluateAsInt(flow.getParameters(),flow.getRecord());
    LineFormatter fieldFormatter = new LineFormatter(fieldWidth, alignment, padCharacter);

    Record record = flow.getRecord();

    String v = record.getString(fieldName);
    if (v == null) {
      v = defaultValueEvaluator.evaluateString(context, flow);
    } else {
      v = v.trim();
      char sign = v.charAt(0); 
      if (sign == '-') {
        v=v.substring(1);
      }
      if (sign == '-') {
        char lastDigit = v.charAt(v.length()-1); 
        char overpunch = negativeEncoded.get(lastDigit);
        v = v.substring(0,v.length()-1) + overpunch;
      }
    }

    //System.out.println(getClass().getName()+".writeField start="+start);
    int offset = flatFileOptions.rebaseIndex(start);
    if (offset >= 0) {
      recordOutput.setPosition(offset);
    } 
    String s = fieldFormatter.format(v);
    recordOutput.writeString(s);
  }

  public void writeEndDelimiterTo(RecordOutput recordOutput) {
  }
}

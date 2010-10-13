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

package com.servingxml.components.recordmapping;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;

/**
 * Implements a <tt>GroupRecognizer</tt>.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FieldGroupRecognizer implements GroupRecognizer {
  private final Name recordTypeName;
  private final SubstitutionExpr[] fieldNames;

  public FieldGroupRecognizer(Name recordTypeName, SubstitutionExpr[] fieldNames) {
    this.recordTypeName = recordTypeName;
    this.fieldNames = fieldNames;
  }

  public boolean startRecognized(ServiceContext context, Flow flow, Record previousRecord, 
                                 Record currentRecord) {

    boolean done = false;
    if (recordTypeName.isEmpty() || currentRecord.getRecordType().getName().equals(recordTypeName)) {
      if (previousRecord == null) {
        done = true;
      } else {
        for (int i = 0; !done && i < fieldNames.length; ++i) {
          SubstitutionExpr expr = fieldNames[i];
          String v = expr.evaluateAsString(Record.EMPTY,currentRecord);
          String u = expr.evaluateAsString(Record.EMPTY,previousRecord);
          if (u == null || !u.equals(v)) {
            done = true;
          }
        }
      }
    }
    return done;
  }

  public boolean endRecognized(ServiceContext context, Flow flow, Record currentRecord, Record nextRecord) {
    boolean done = false;

    if (nextRecord == null) {
      done = true;
    } else {
      if (recordTypeName.isEmpty() || currentRecord.getRecordType().getName().equals(recordTypeName)) {
        for (int i = 0; !done && i < fieldNames.length; ++i) {
          SubstitutionExpr expr = fieldNames[i];
          String v = expr.evaluateAsString(Record.EMPTY,currentRecord);
          String u = expr.evaluateAsString(Record.EMPTY,nextRecord);
          if (u == null || !u.equals(v)) {
            done = true;
          }
        }
      }
    }
    return done;
  }
}


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

package com.servingxml.components.recordio;

import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.components.common.Validator;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


public class FieldValidator implements Validator {
  private final Name fieldName;
  private final Restriction valueValidation;
  private final Validator[] recordValidators;
  private final SubstitutionExpr messageExpr;

  public FieldValidator(Name fieldName, Restriction valueValidation, 
                       Validator[] recordValidators, SubstitutionExpr messageExpr) {
    this.fieldName = fieldName;
    this.valueValidation = valueValidation;
    this.recordValidators = recordValidators;
    this.messageExpr = messageExpr;
  }

  public boolean validate(ServiceContext context, Flow flow, List<String> failures) {

    boolean success = true;

    Record record = flow.getRecord();
    Value value = record.getValue(fieldName);
    if (value != null) {
      Record[] subRecords = value.getRecords();
      if (subRecords.length > 0) {
        for (int i = 0; i < recordValidators.length; ++i) {
          Validator recordValidation = recordValidators[i];
          for (int j = 0; j < subRecords.length; ++j) {
            Record subRecord = subRecords[j];
            Flow newFlow = flow.replaceRecord(context,subRecord);
            try {
              if (!recordValidation.validate(context, newFlow, failures)) {
                success = false;
              }
            } catch (Exception e) {
              failures.add(e.getMessage());
              success = false;
            }
          }
        }
      }
      try {
        if (!valueValidation.accept(context,flow,value)) {
          success = false;
        }
      } catch (Exception e) {
        failures.add(e.getMessage());
        success = false;
      }
      //System.out.println(getClass().getName()+".validate success="+success);
    }
    if (!success) {
      String message = messageExpr.evaluateAsString(flow.getParameters(),record);
      failures.add(message);
    }
    return success;
  }
}

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
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.components.common.Validator;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


public class ValidateRecord implements Validator {
  private final Name recordTypeName;
  private final Validator[] validators;
  private final SubstitutionExpr messageExpr;

  public ValidateRecord(Name recordTypeName, Validator[] validators, SubstitutionExpr messageExpr) {
    this.recordTypeName = recordTypeName;
    this.validators = validators;
    this.messageExpr = messageExpr;
  }

  public boolean validate(ServiceContext context, Flow flow, List<String> failures) {

    boolean success = true;

    Record record = flow.getRecord();
    if (recordTypeName.isEmpty() || recordTypeName.equals(record.getRecordType().getName())) {
      for (int i = 0; i < validators.length; ++i) {
        try {
          if (!validators[i].validate(context, flow, failures)) {
            success = false;
          }
        } catch (Exception e) {
          failures.add(e.getMessage());
          success = false;
        }
      }

      if (!success) {
        String message = messageExpr.evaluateAsString(flow.getParameters(),record);
        failures.add(0, message);
      }
    }

    return success;
  }
}

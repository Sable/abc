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

import java.util.ArrayList;
import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.BooleanOperatorEnum;
import com.servingxml.components.common.Validator;
import com.servingxml.components.regex.PatternMatcherFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordValidatorAppenderAssembler {
  private Name recordTypeName = Name.EMPTY;
  private String message = "";
  private Validator[] validators = new Validator[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;

  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Validator[] validators) {
    this.validators = validators;
  }

  public RecordFilterValidatorAppender assemble(ConfigurationContext context) {

    if (validators.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_SPECIALIZATION_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:validator", "sx:fieldValidator, msv:schemaValidator");
      throw new ServingXmlException(message);
    }

    SubstitutionExpr messageExpr = SubstitutionExpr.parseString(context.getQnameContext(),message);

    RecordFilterValidatorAppender recordFilterAppender = new RecordValidatorAppender(recordTypeName, validators, messageExpr);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterValidatorAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class RecordValidatorAppender extends AbstractRecordFilterAppender     
implements RecordFilterValidatorAppender {
  private final Name recordTypeName;
  private final Validator[] validators;
  private final SubstitutionExpr messageExpr;

  public RecordValidatorAppender(Name recordTypeName, 
                                 Validator[] validators, 
                                 SubstitutionExpr messageExpr) {
    this.recordTypeName = recordTypeName;
    this.validators = validators;
    this.messageExpr = messageExpr;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    RecordFilter recordFilter = new RecordValidator(recordTypeName, validators, messageExpr); 
    pipeline.addRecordFilter(recordFilter);
  }

  public boolean validate(ServiceContext context, Flow flow, List<String> failures) {
    Validator recordFilter = new RecordValidator(recordTypeName, validators, messageExpr); 
    return recordFilter.validate(context,flow,failures);
  }
}

class RecordValidator extends AbstractRecordFilter implements Validator {
  private final Name recordTypeName;
  private final Validator[] validators;
  private final SubstitutionExpr messageExpr;

  public RecordValidator(Name recordTypeName, 
                         Validator[] validators, 
                         SubstitutionExpr messageExpr) {
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

  public void writeRecord(ServiceContext context, Flow flow) {

    try {
      List<String> failures = new ArrayList<String>();
      boolean success = validate(context, flow, failures);
      if (!success) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < failures.size(); ++i) {
          stringBuilder.append(failures.get(i).trim());
          stringBuilder.append(" ");
        }
        String message = stringBuilder.toString();
        //System.out.println(getClass().getName()+".writeRecord "+message);
        throw new ServingXmlException(message);
      }
      
      super.writeRecord(context,flow);
    } catch (Exception e) {
      ServingXmlException reason = new ServingXmlException(e.getMessage(), e);
      discardRecord(context, flow, reason);
    }
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    super.startRecordStream(context, flow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    super.endRecordStream(context, flow);
  }
}

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

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.BooleanOperatorEnum;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class RestrictRecordFilterAppenderAssembler {
  private Restriction[] restrictions = Restriction.EMPTY_ARRAY;
  private String operator = BooleanOperatorEnum.OR.toString();
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private UnmatchedRecord unmatchedRecord = UnmatchedRecord.NULL;

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Restriction[] restrictions) {
    this.restrictions = restrictions;
  }

  public void injectComponent(UnmatchedRecord unmatchedRecord) {
    this.unmatchedRecord = unmatchedRecord;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    
    if (restrictions.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:regexCriteria, sx:restriction");
      throw new ServingXmlException(message);
    }

    BooleanOperatorEnum operatorEnum;
    try {
      operatorEnum = BooleanOperatorEnum.parse(operator);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "operator");
      e = e.supplementMessage(message);
      throw e;
    }
    
    RecordFilterAppender recordFilterAppender = new RestrictRecordFilterAppender(restrictions, operatorEnum,
      unmatchedRecord);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class RestrictRecordFilterAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender {
  private final Restriction[] restrictions;
  private final BooleanOperatorEnum operatorEnum;
  private final UnmatchedRecord unmatchedRecord;
  
  public RestrictRecordFilterAppender(Restriction[] restrictions, BooleanOperatorEnum operatorEnum,
    UnmatchedRecord unmatchedRecord) {
    this.restrictions = restrictions;
    this.operatorEnum = operatorEnum;
    this.unmatchedRecord = unmatchedRecord;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter unmatchedRecordFilter = unmatchedRecord.createRecordFilter(context, flow);
    RecordFilter recordFilter = new RestrictRecordFilter(restrictions, operatorEnum, unmatchedRecordFilter); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class RestrictRecordFilter extends AbstractRecordFilter {
  private final Restriction[] restrictions;
  private final BooleanOperatorEnum operatorEnum;
  private final RecordFilter unmatchedRecordFilter;

  public RestrictRecordFilter(Restriction[] restrictions, BooleanOperatorEnum operatorEnum,
    RecordFilter unmatchedRecordFilter) {
    this.restrictions = restrictions;
    this.operatorEnum = operatorEnum;
    this.unmatchedRecordFilter = unmatchedRecordFilter;
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    boolean accept = true;

    if (restrictions.length > 0) {
      accept = restrictions[0].accept(context,flow,Value.EMPTY);
      if (operatorEnum.isAnd()) {
        for (int i = 1; accept && i < restrictions.length; ++i) {
          Restriction field = restrictions[i];
          accept = field.accept(context,flow,Value.EMPTY);
        }
      } else {
        for (int i = 1; !accept && i < restrictions.length; ++i) {
          Restriction field = restrictions[i];
          accept = field.accept(context,flow,Value.EMPTY);
        }
      }
    }

    if (accept) {
      getRecordWriter().writeRecord(context, flow);
    } else {
      unmatchedRecordFilter.writeRecord(context, flow);
    }
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    super.startRecordStream(context, flow);
    unmatchedRecordFilter.startRecordStream(context, flow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    unmatchedRecordFilter.endRecordStream(context, flow);
    super.endRecordStream(context, flow);
  }
}

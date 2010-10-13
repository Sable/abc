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
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.Stack;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.components.common.NameSubstitutionExpr;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

class ComposeRecord extends AbstractRecordFilter {
  private final GroupRecognizer groupRecognizer;
  private Name compositeRecordTypeName;
  private final Stack<Flow> stack = new Stack<Flow>();
  private boolean started = false;
  private final NameSubstitutionExpr recordTypeEvaluator;
  private final Name repeatingGroupName;
  private final NewField[] newFields;
  private Record previousRecord;
  private Flow currentFlow;

  public ComposeRecord(NameSubstitutionExpr recordTypeEvaluator, Name repeatingGroupName, 
                       GroupRecognizer groupRecognizer, NewField[] newFields) {
    this.recordTypeEvaluator = recordTypeEvaluator;
    this.repeatingGroupName = repeatingGroupName;
    this.groupRecognizer = groupRecognizer;
    this.newFields = newFields;
  }

  public void writeRecord(ServiceContext context, Flow nextFlow) {
    //System.out.println(getClass().getName()+".writeRecord started="+started);

    if (currentFlow != null) {
      Record currentRecord = currentFlow.getRecord();
      if (!started) {
        started = groupRecognizer.startRecognized(context, currentFlow, 
                                                  previousRecord, 
                                                  currentRecord);
        if (started) {
          compositeRecordTypeName = recordTypeEvaluator.evaluateName(currentFlow.getParameters(),
                                                                     currentRecord);
        }
        //if (started) {
        //System.out.println(getClass().getName()+"start recognized!");
        //}
      }
      if (started) {
        if (nextFlow != null) { 
            started = !groupRecognizer.endRecognized(context,
                                                     nextFlow, 
                                                     currentRecord, 
                                                     nextFlow.getRecord());
        } else {
            started = false;
        }
        stack.push(currentFlow);
        //if (!started) {
        //System.out.println(getClass().getName()+"end recognized!");
        //}
        if (!started) {
          writeStack(context);
        } 
        //else {
        //  stack.push(currentFlow);
        //}
      } else {
        //String s = currentFlow.getRecord().toXmlString(context);
        //System.out.println("writeRecord " + s);
        super.writeRecord(context,currentFlow);
      }
      previousRecord = currentFlow.getRecord();
    }
    //System.out.println(getClass().getName()+".writeRecord leave started="+started);

    currentFlow = nextFlow;
  }

  private void writeStack(ServiceContext context) {

    if (!stack.empty()) {
      //System.out.println("compositeRecordType="+compositeRecordTypeName);
      RecordBuilder recordBuilder = new RecordBuilder(compositeRecordTypeName);
      Record[] segments = new Record[stack.size()];
      for (int i = 0; !stack.empty(); ++i) {
        Flow flow = stack.dequeue();
        segments[i] = flow.getRecord();
      }
      recordBuilder.setRecords(repeatingGroupName,segments);
      Flow flow = currentFlow.replaceRecord(context, recordBuilder.toRecord());

      if (newFields.length > 0) {
        for (int i = 0; i < newFields.length; ++i) {
          NewField newField = newFields[i];
          newField.readField(context, flow, recordBuilder);
        }
        Record compositeRecord = recordBuilder.toRecord();
        flow = currentFlow.replaceRecord(context, compositeRecord);
      }
      //System.out.println(recordBuilder.toRecord().toXmlString(context));
      //String s = flow.getRecord().toXmlString(context);
      //System.out.println("writeStack " + s);
      super.writeRecord(context, flow);
    }
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    super.startRecordStream(context, flow);
    this.previousRecord = null;
    this.currentFlow = null;     
    this.started = false;
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    writeRecord(context, null);
    super.endRecordStream(context, flow);
  }
}

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

import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;
import com.servingxml.util.NameTest;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.FieldType;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.RecordType;
import com.servingxml.util.record.Value;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordProjectionAppenderAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name recordTypeName = Name.EMPTY;
  private Name newRecordTypeName = Name.EMPTY;
  private String fieldQnames = "";
  private String exceptQnames = "";

  public void setRecordType(Name recordType) {
    this.recordTypeName = recordType;
  }

  public void setNewRecordType(Name newRecordTypeName) {
    this.newRecordTypeName = newRecordTypeName;
  }

  public void setFields(String fieldQnames) {
    this.fieldQnames = fieldQnames;
  }

  public void setExceptFields(String exceptQnames) {
    this.exceptQnames = exceptQnames;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    NameTest fields = NameTest.parse(context.getQnameContext(), fieldQnames);
    NameTest exceptFields = NameTest.parse(context.getQnameContext(), exceptQnames);

    RecordAccepterFactory accepterFactory = RecordAccepterFactory.newInstance(recordTypeName);
    RecordAccepter recordAccepter = accepterFactory.createRecordAccepter();
    
    RecordFilterAppender recordFilterAppender = new RecordProjectionAppender(recordAccepter, 
                                                                             newRecordTypeName,
                                                                             fields,
                                                                             exceptFields);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}

class RecordProjectionAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender {
  private final RecordAccepter recordAccepter;
  private final Name newRecordTypeName;
  private final NameTest fields;
  private final NameTest exceptFields;
  
  public RecordProjectionAppender(RecordAccepter recordAccepter, Name newRecordTypeName, 
                                  NameTest fields, NameTest exceptFields) {
    this.recordAccepter = recordAccepter;
    this.newRecordTypeName = newRecordTypeName;
    this.fields = fields;
    this.exceptFields = exceptFields;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new RecordProjection(recordAccepter, newRecordTypeName, fields, exceptFields); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class RecordProjection extends AbstractRecordFilter {
  private final RecordAccepter recordAccepter;
  private final Name newRecordTypeName;
  private final NameTest fields;
  private final NameTest exceptFields;
  private Set<FlowWrapper> recordSet = new HashSet<FlowWrapper>();

  public RecordProjection(RecordAccepter recordAccepter, Name newRecordTypeName, 
                                  NameTest fields, NameTest exceptFields) {
    this.recordAccepter = recordAccepter;
    this.newRecordTypeName = newRecordTypeName;
    this.fields = fields;
    this.exceptFields = exceptFields;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    if (recordAccepter.accept(context,flow,Value.EMPTY)) {
      Record record = flow.getRecord();
      Name recordTypeName = newRecordTypeName.isEmpty() ? record.getRecordType().getName() : newRecordTypeName;
  
      final RecordBuilder recordBuilder = new RecordBuilder(recordTypeName);
  
      RecordType recordType = record.getRecordType();
      for (int i = 0; i < recordType.count(); ++i) {
        FieldType fieldType = recordType.getFieldType(i);
        Value value = record.getValue(i);
        Name fieldName = fieldType.getName();
        if (fields.matches(fieldName) && !exceptFields.matches(fieldName)) {
          recordBuilder.setValue(fieldName, value);
        }
      }
  
      Flow newFlow = flow.replaceRecord(context,recordBuilder.toRecord());
      recordSet.add(new FlowWrapper(newFlow));
    } else {
      super.writeRecord(context,flow);
    }
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    if (recordSet.size() > 0) {
      FlowWrapper[] entries = new FlowWrapper[recordSet.size()];
      entries = recordSet.toArray(entries);
      for (int i = 0; i < entries.length; ++i) {
        super.writeRecord(context, entries[i].flow);
      }
    }

    super.endRecordStream(context, flow);
  }

  static final class FlowWrapper {
    Flow flow;
    private final int hash;

    FlowWrapper(Flow flow) {
      this.flow = flow;
      Record record = flow.getRecord();

      int hc = 0;
      for (int i = 0; i < record.fieldCount(); ++i) {
        Value value = record.getValue(i);
        hc = hc + 31*value.hashCode();
      }
      this.hash = hc;
    }

    public int hashCode() {
      return hash;
    }

    public boolean equals(Object obj) {
      boolean equal = false;
      if (obj instanceof FlowWrapper) {
        FlowWrapper fw = (FlowWrapper)obj;
        Record record = flow.getRecord();
        Record aRecord = fw.flow.getRecord();
        if (record.fieldCount() == aRecord.fieldCount()) {
          equal = true;
          for (int i = 0; equal && i < record.fieldCount(); ++i) {
            Value value = record.getValue(i);
            Value aValue = aRecord.getValue(record.getFieldName(i));
            if (aValue != null) {
              equal = value.equalsValue(aValue);
            } else {
              equal = false;
            }
          }
        }
      }
      return equal;
    }
  }
}

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

import com.servingxml.app.ServiceContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.Flow;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public interface UnmatchedRecord {
  public static final UnmatchedRecord NULL = new NullUnmatchedRecord();

  RecordFilter createRecordFilter(ServiceContext context, Flow flow) 
  ;
}

class UnmatchedRecordImpl implements UnmatchedRecord {
  private final RecordPipelineAppender[] recordPipelineAppenders;

  public UnmatchedRecordImpl(RecordPipelineAppender[] recordPipelineAppenders) {
    this.recordPipelineAppenders = recordPipelineAppenders;
  }

  public RecordFilter createRecordFilter(ServiceContext context, Flow flow) {

    //RecordReader recordReader = new SplitRecordReader();
    //RecordPipeline subPipeline = new RecordPipeline(recordReader);
    //for (int i = 0; i < recordPipelineAppenders.length; ++i) {
    //  RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[i];
    //  recordPipelineAppender.appendToRecordPipeline(context,flow,subPipeline);
    //}
    MultipleRecordFilter recordFilter = new MultipleRecordFilter();
    for (int i = 0; i < recordPipelineAppenders.length; ++i) {
      RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[i];
      recordPipelineAppender.appendToRecordPipeline(context, flow, recordFilter);
    }

    return recordFilter;
  }

}

final class NullUnmatchedRecord implements UnmatchedRecord {
  public final RecordFilter createRecordFilter(ServiceContext context, Flow flow) {
    return RecordFilter.NULL;
  }
}


class UnmatchedRecordPrefilter implements UnmatchedRecord {
  private final UnmatchedRecord unmatchedRecord;
  private final ParameterDescriptor[] parameterDescriptors;

  public UnmatchedRecordPrefilter(UnmatchedRecord unmatchedRecord,
    ParameterDescriptor[] parameterDescriptors) {
    this.unmatchedRecord = unmatchedRecord;
    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordFilter createRecordFilter(ServiceContext context, Flow flow) {

    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return unmatchedRecord.createRecordFilter(context, newFlow);
  }

}


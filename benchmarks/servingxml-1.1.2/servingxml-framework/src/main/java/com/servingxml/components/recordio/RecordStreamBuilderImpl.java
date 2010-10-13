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
import com.servingxml.components.task.AbstractTask;

/**
 *
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class RecordStreamBuilderImpl extends AbstractTask
  implements RecordStreamBuilder {

  private final RecordPipelineAppender[] recordPipelineAppenders;

  //  Preconditions:  
  //    recordPipelineAppenders.length > 0
  public RecordStreamBuilderImpl(RecordPipelineAppender[] recordPipelineAppenders) {
    this.recordPipelineAppenders = recordPipelineAppenders;
  }

  public RecordPipeline createRecordPipeline(ServiceContext context, Flow flow) {
    //System.out.println("RecordStreamBuilderImpl.createRecordPipeline Enter");

    RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[0];
    RecordPipeline pipeline = recordPipelineAppender.createRecordPipeline(context, flow);
    for (int i = 1; i < recordPipelineAppenders.length; ++i) {
      recordPipelineAppender = recordPipelineAppenders[i];
      recordPipelineAppender.appendToRecordPipeline(context, flow, pipeline);
    }
    return pipeline;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
    RecordFilterChain pipeline) {

    for (int i = 0; i < recordPipelineAppenders.length; ++i) {
      RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[i];
      recordPipelineAppender.appendToRecordPipeline(context,flow,pipeline);
    }
  }

  public void execute(ServiceContext context, Flow flow) {

    RecordPipeline recordPipeline = createRecordPipeline(context, flow);
    recordPipeline.execute(context);
  }
}


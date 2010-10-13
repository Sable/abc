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
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class RecordPipelineAppenderPrefilter    
implements RecordPipelineAppender {
  private final RecordPipelineAppender recordFilterAppender;
  private final ParameterDescriptor[] parameterDescriptors;

  public RecordPipelineAppenderPrefilter(RecordPipelineAppender recordFilterAppender,
  ParameterDescriptor[] parameterDescriptors) {
    this.recordFilterAppender = recordFilterAppender;
    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordPipeline createRecordPipeline(ServiceContext context, Flow flow) {

    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    RecordPipeline pipeline = recordFilterAppender.createRecordPipeline(context, newFlow);
    RecordFilter prefilter = new RecordFilterPrefilter(parameterDescriptors);
    pipeline.addRecordFilter(prefilter);
    return pipeline;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    RecordFilter prefilter = new RecordFilterPrefilter(parameterDescriptors);
    pipeline.addRecordFilter(prefilter);
    recordFilterAppender.appendToRecordPipeline(context, newFlow, pipeline);
  }
}


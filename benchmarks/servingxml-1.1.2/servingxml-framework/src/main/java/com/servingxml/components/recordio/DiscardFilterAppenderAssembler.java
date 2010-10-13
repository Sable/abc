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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class DiscardFilterAppenderAssembler {
 
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private RecordPipelineAppender[] recordPipelineAppenders = RecordPipelineAppender.EMPTY_ARRAY;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(RecordPipelineAppender[] recordPipelineAppenders) {
    this.recordPipelineAppenders = recordPipelineAppenders;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    RecordFilterAppender recordFilterAppender = new DiscardFilterAppender(recordPipelineAppenders);
    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }

    return recordFilterAppender;

  }
}


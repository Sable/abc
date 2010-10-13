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
import com.servingxml.app.Flow;
import com.servingxml.util.Name;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class DiscardFilterAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender { 
  private final RecordPipelineAppender[] recordPipelineAppenders;

  public DiscardFilterAppender(RecordPipelineAppender[] recordPipelineAppenders) {
    this.recordPipelineAppenders = recordPipelineAppenders;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    MultipleRecordFilter recordFilter = new MultipleRecordFilter();
    for (int i = 0; i < recordPipelineAppenders.length; ++i) {
      RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[i];
      recordPipelineAppender.appendToRecordPipeline(context, flow, recordFilter);
    }
    //System.out.println(getClass().getName()+".appendToRecordPipeline " + recordFilter.getClass().getName());

    pipeline.setDiscardWriter(recordFilter);
  }
}


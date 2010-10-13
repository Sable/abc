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

package com.servingxml.components.choose;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.task.Task;
import com.servingxml.components.recordio.RecordPipelineAppender;
import com.servingxml.components.content.Content;
import com.servingxml.app.Flow;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.string.StringFactory;

/**
 * Implements an <code>Alternative</code>.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Alternative
 */

class OtherwiseAlternative extends AbstractAlternative implements Alternative {

  public OtherwiseAlternative(ParameterDescriptor[] parameterDescriptors,
    Task[] tasks, 
    Content[] xmlComponents,
    RecordPipelineAppender[] recordPipelineAppenders,
    StringFactory stringFactory,
    MapXmlFactory recordMapFactory) {
    super(parameterDescriptors, tasks, xmlComponents, recordPipelineAppenders, 
      stringFactory, recordMapFactory);
  }

  public final boolean test(ServiceContext context, Flow flow) {
    return true;
  }

  public String getTest() {
    return "";
  }
}

                      


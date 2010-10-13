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

package com.servingxml.components.sql;

import java.sql.Connection;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.ParameterDescriptor;

public class SqlBatchUpdateOperationPrefilter implements SqlBatchUpdateOperation {
  private final SqlBatchUpdateOperation sqlUpdater;
  private final ParameterDescriptor[] parameterDescriptors;

  public SqlBatchUpdateOperationPrefilter(SqlBatchUpdateOperation sqlUpdater, ParameterDescriptor[] parameterDescriptors) {
    this.sqlUpdater = sqlUpdater;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void startUpdate(ServiceContext context, Flow flow, Connection connection) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    sqlUpdater.startUpdate(context, newFlow, connection);
  }

  public void update(ServiceContext context, Flow[] flowHistory, Connection connection) {
    Flow[] newFlowHistory = new Flow[flowHistory.length];
    for (int i = 0; i < flowHistory.length; ++i) {
      newFlowHistory[i] = flowHistory[i].augmentParameters(context,parameterDescriptors);
    }
    sqlUpdater.update(context, newFlowHistory, connection);
  }

  public void endUpdate(ServiceContext context, Flow flow, Connection connection) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    sqlUpdater.endUpdate(context, newFlow, connection); 
  }
}


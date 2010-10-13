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

package com.servingxml.components.content.dynamic;

import com.servingxml.components.cache.ExpirableFactory;
import com.servingxml.io.cache.Expirable;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class DynamicChangeableFactory implements ExpirableFactory {

  private final RecordMetaData recordMetaData;
  private final DynamicChangeable dynamicExpirable;

  public DynamicChangeableFactory(DynamicChangeable dynamicExpirable,
  RecordMetaData recordMetaData) {
    this.dynamicExpirable = dynamicExpirable;
    this.recordMetaData = recordMetaData;
  }
  public Expirable createExpirable(ServiceContext context, Flow flow) {
    Object parametersProxy = recordMetaData.createRecordProxy(flow.getParameters());
    return new DynamicChangeableExpirable(dynamicExpirable,parametersProxy);
  }
}

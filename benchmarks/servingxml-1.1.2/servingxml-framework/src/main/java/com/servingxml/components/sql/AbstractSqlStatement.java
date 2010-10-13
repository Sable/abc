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

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

public abstract class AbstractSqlStatement implements SqlStatement {
  public abstract void buildSql(ServiceContext context, Flow flow,
  StringBuilder buf);
  
  public String buildSql(ServiceContext context, Flow flow) {
    StringBuilder buf = new StringBuilder();
    buildSql(context, flow, buf);
    return buf.toString();
  }
}

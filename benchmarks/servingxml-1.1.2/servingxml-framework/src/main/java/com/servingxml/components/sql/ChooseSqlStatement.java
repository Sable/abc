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

public class ChooseSqlStatement extends AbstractSqlStatement {
  private final AlternativeSqlStatement[] alternatives;
  private final AbstractSqlStatement tail;
  
  public ChooseSqlStatement(AlternativeSqlStatement[] alternatives, 
  AbstractSqlStatement tail) {
    this.alternatives = alternatives;
    this.tail = tail;
  }
  
  public void buildSql(ServiceContext context, Flow flow,
  StringBuilder buf) {
    if (tail != null) {
      tail.buildSql(context, flow, buf);
    }
    boolean testsTrue = false;
    for (int i = 0; !testsTrue && i < alternatives.length; ++i) {
      AlternativeSqlStatement alternative = alternatives[i];
      testsTrue = alternative.testsTrue(context, flow);
      if (testsTrue) {
        alternative.buildSql(context, flow, buf);
      }
    }
  }
}

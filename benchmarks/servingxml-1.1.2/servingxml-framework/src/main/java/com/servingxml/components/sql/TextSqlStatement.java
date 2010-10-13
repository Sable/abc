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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.expr.substitution.SubstitutionExpr;

public class TextSqlStatement extends AbstractSqlStatement {
  private final SubstitutionExpr subExpr;
  private final AbstractSqlStatement tail;
  
  public TextSqlStatement(SubstitutionExpr subExpr, 
  AbstractSqlStatement tail) {
    this.subExpr = subExpr;
    this.tail = tail;
  }
  
  public void buildSql(ServiceContext context, Flow flow,
  StringBuilder buf) {
    if (tail != null) {
      tail.buildSql(context, flow, buf);
    }
    String s = subExpr.evaluateAsString(flow.getParameters(), flow.getRecord());
    buf.append(s);
  }
}

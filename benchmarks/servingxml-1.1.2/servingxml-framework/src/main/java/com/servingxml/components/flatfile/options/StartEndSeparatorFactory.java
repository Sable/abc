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

package com.servingxml.components.flatfile.options;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.expr.substitution.SubstitutionExpr;

public class StartEndSeparatorFactory implements SeparatorFactory {
  private final SubstitutionExpr startExpr;
  private final SubstitutionExpr endExpr;

  public StartEndSeparatorFactory(SubstitutionExpr startExpr, SubstitutionExpr endExpr) {
    this.startExpr = startExpr;
    this.endExpr = endExpr;
  }

  public Separator createSeparator(ServiceContext context, Flow flow) {
    String start = startExpr.evaluateAsString(flow.getParameters(), flow.getRecord());
    String end = endExpr.evaluateAsString(flow.getParameters(), flow.getRecord());

    return new StartEndSeparator(start, end);
  }
}

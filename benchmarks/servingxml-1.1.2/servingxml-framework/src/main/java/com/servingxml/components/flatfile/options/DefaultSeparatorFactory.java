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
import com.servingxml.expr.substitution.LiteralSubstitutionExpr;

public class DefaultSeparatorFactory implements SeparatorFactory {
  private final SubstitutionExpr valueExpr;
  private final SubstitutionExpr escapedByExpr;
  private final SubstitutionExpr continuationSequenceExpr;

  public DefaultSeparatorFactory(SubstitutionExpr valueExpr, 
                                 SubstitutionExpr escapedByExpr,
                                 SubstitutionExpr continuationSequenceExpr) {
    this.valueExpr = valueExpr;
    this.escapedByExpr = escapedByExpr;
    this.continuationSequenceExpr = continuationSequenceExpr;
  }

  public DefaultSeparatorFactory(SubstitutionExpr valueExpr, 
                                 SubstitutionExpr escapedByExpr) {
    this.valueExpr = valueExpr;
    this.escapedByExpr = escapedByExpr;
    this.continuationSequenceExpr = SubstitutionExpr.EMPTY;
  }

  public DefaultSeparatorFactory(SubstitutionExpr valueExpr) {
    this.valueExpr = valueExpr;
    this.escapedByExpr = SubstitutionExpr.EMPTY;
    this.continuationSequenceExpr = SubstitutionExpr.EMPTY;
  }

  public DefaultSeparatorFactory(String separator) {
    this.valueExpr = new LiteralSubstitutionExpr(separator);
    this.escapedByExpr = SubstitutionExpr.EMPTY;
    this.continuationSequenceExpr = SubstitutionExpr.EMPTY;
  }

  public Separator createSeparator(ServiceContext context, Flow flow) {
    String value = valueExpr.evaluateAsString(flow.getParameters(), flow.getRecord());
    String escapedBy = escapedByExpr.evaluateAsString(flow.getParameters(), flow.getRecord());
    String continuationSequence = continuationSequenceExpr.evaluateAsString(flow.getParameters(), flow.getRecord());

    return new DefaultSeparator(value, escapedBy, continuationSequence);
  }
}

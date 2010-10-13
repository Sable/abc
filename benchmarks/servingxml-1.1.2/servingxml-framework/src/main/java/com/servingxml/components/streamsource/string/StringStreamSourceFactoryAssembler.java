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

package com.servingxml.components.streamsource.string;

import org.w3c.dom.Element;

import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.expr.substitution.LiteralSubstitutionExpr;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.DomHelper;

public class StringStreamSourceFactoryAssembler {

  private String value = null;

  public void setValue(String value) {
    this.value = value;
  }

  public StreamSourceFactory assemble(ConfigurationContext context) {

    ValueEvaluator valueEvaluator;
    if (value != null) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
      valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
    } else {
      StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
      valueEvaluator = new StringValueEvaluator(stringFactory);
    }

    StreamSourceFactory streamSourceFactory = new StringStreamSourceFactory(valueEvaluator);

    return streamSourceFactory;
  }
}

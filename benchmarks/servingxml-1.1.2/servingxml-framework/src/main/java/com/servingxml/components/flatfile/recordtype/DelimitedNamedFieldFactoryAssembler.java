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

package com.servingxml.components.flatfile.recordtype;

import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.components.label.Label;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;

public class DelimitedNamedFieldFactoryAssembler extends FlatFileOptionsFactoryAssembler {
  
  private String fieldLabel = null;
  private int start = -1;
  private int maxFieldWidth = -1;
  private int minLength = 0;
  private int maxLength = -1;
  private DefaultValue defaultValueEvaluator = DefaultValue.EMPTY;
  private Label label = null;
  
  public void setStart(int start) {
    this.start = start;
  }
  
  public void setWidth(int maxFieldWidth) {
    this.maxFieldWidth = maxFieldWidth;
  }

  public void setMaxWidth(int maxFieldWidth) {
    this.maxFieldWidth = maxFieldWidth;
  }

  public void setMinLength(int minLength) {
    this.minLength = minLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }
  
  public void setLabel(String fieldLabel) {
    this.fieldLabel = fieldLabel;
  }                                                             

  public void injectComponent(DefaultValue defaultValueEvaluator) {
    this.defaultValueEvaluator = defaultValueEvaluator;
  }

  public void injectComponent(Label label) {
    this.label = label;
  }

  public FlatRecordFieldFactory assemble(ConfigurationContext context) {

    // Revisit, may need to delay, use flatFileOptions.isQuote()
    if (maxFieldWidth >= 0 && maxLength < maxFieldWidth) {
      maxLength = maxFieldWidth;
    }

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    if (label == null) {
      if (fieldLabel != null) {
        SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),fieldLabel);
        ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
        label = new Label(valueEvaluator);
      } else {
        label = new Label("");
      }
    }

    FlatRecordFieldFactory fieldTypeFactory = new DelimitedNamedFieldFactory(label,start,maxFieldWidth,
      minLength, maxLength, defaultValueEvaluator, flatFileOptionsFactory);

    return fieldTypeFactory;
  }
}


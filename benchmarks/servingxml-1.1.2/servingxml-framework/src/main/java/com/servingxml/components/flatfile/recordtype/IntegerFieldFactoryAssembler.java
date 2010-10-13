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
import com.servingxml.components.label.Label;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.components.common.TrueFalseEnum;

public class IntegerFieldFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private Name fieldName = Name.EMPTY;
  private String fieldLabel = null;
  private int start = -1;
  private int width = -1;
  private DefaultValue defaultValue = DefaultValue.EMPTY;
  private Label label = null;
  private String bigEndian = TrueFalseEnum.TRUE.toString();

  public void setStart(int start) {
    this.start = start;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setName(Name fieldName) {
    this.fieldName = fieldName;
  }                                                             

  public void setLabel(String fieldLabel) {
    this.fieldLabel = fieldLabel;
  }                                                             

  public void setBigEndian(String bigEndian) {
    this.bigEndian = bigEndian;
  }                                                             

  public void injectComponent(DefaultValue defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void injectComponent(Label label) {
    this.label = label;
  }

  public FlatRecordFieldFactory assemble(ConfigurationContext context) {

    if (fieldName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                         context.getElement().getTagName(),"name");
      throw new ServingXmlException(message);
    }

    if (width <= 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                         context.getElement().getTagName(),"width");
      throw new ServingXmlException(message);
    }
    if (!(width == 1 || width == 2 || width == 4 || width == 8)) {
      String message = "Integer field width must be 1, 2, 4 or 8";
      throw new ServingXmlException(message);
    }

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    if (label == null) {
      if (fieldLabel != null) {
        SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),fieldLabel);
        ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
        label = new Label(valueEvaluator);
      } else {
        label = new Label(fieldName.getLocalName());
      }
    }

    TrueFalseEnum bigEndianIndicator;
    try {
      bigEndianIndicator = TrueFalseEnum.parse(bigEndian);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
                                                                 context.getElement().getTagName(), "bigEndian");
      e = e.supplementMessage(message);
      throw e;
    }

    FlatRecordFieldFactory fieldTypeFactory = new IntegerFieldFactory(fieldName,label,
                                                start,width,
                                                defaultValue,bigEndianIndicator.booleanValue(),flatFileOptionsFactory);

    return fieldTypeFactory;
  }
}


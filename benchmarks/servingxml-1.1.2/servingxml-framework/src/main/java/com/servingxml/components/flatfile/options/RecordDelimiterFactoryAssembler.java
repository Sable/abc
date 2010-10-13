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

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.StringHelper;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * The <code>RecordDelimiterFactoryAssembler</code> implements an assembler for
 * assembling <code>RecordDelimiter</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordDelimiterFactoryAssembler {

  private String start = "";
  private String end = "";
  private String value = null;
  private String continuationChar = "";
  private String continuationSequence = "";
  private String escapedBy = "";
  private SeparatorFactory separatorFactory = null;
  private String readOnly = TrueFalseEnum.FALSE.toString();
  private String writeOnly = TrueFalseEnum.FALSE.toString();

  public void setStart(String start) {
    this.start = start;
  }

  public void setEnd(String end) {
    this.end = end;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setReadOnly(String readOnly) {
    this.readOnly = readOnly;
  }

  public void setWriteOnly(String writeOnly) {
    this.writeOnly = writeOnly;
  }

  public void setEscapeCharacter(String escapedBy) {
    this.escapedBy = escapedBy;
  }

  public void setContinuation(String continuationChar) {
    this.continuationChar = continuationChar;
  }

  public void setContinuationSequence(String continuationSequence) {
    this.continuationSequence = continuationSequence;
  }

  public void injectComponent(SeparatorFactory separatorFactory) {
    this.separatorFactory = separatorFactory;
  }

  public RecordDelimiterFactory assemble(ConfigurationContext context) {

    if (separatorFactory == null) {
      if (start.length() > 0) {
        if (end.length() == 0) {
          String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"end");
          throw new ServingXmlException(message);
        }

        SubstitutionExpr startExpr = SubstitutionExpr.parseString(context.getQnameContext(),start);
        SubstitutionExpr endExpr = SubstitutionExpr.parseString(context.getQnameContext(),end);
        separatorFactory = new StartEndSeparatorFactory(startExpr, endExpr);
      } else {
        if (value == null) {
          String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"value");
          throw new ServingXmlException(message);
        }
        value = StringHelper.translateEscapeChars(value);
        if (continuationSequence.length() == 0) {
          continuationSequence = continuationChar.length() > 0 ? continuationChar+value : "";
        }

        continuationSequence = StringHelper.translateEscapeChars(continuationSequence);
        //System.out.println(getClass().getName()+".continuationSequence="+continuationSequence+".");

        SubstitutionExpr valueExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
        SubstitutionExpr continuationSequenceExpr = SubstitutionExpr.parseString(context.getQnameContext(),continuationSequence);
        SubstitutionExpr escapedByExpr = SubstitutionExpr.parseString(context.getQnameContext(),escapedBy);
        separatorFactory = new DefaultSeparatorFactory(valueExpr, escapedByExpr, continuationSequenceExpr);
      }
    }

    TrueFalseEnum readOnlyIndicator;
    try {
      readOnlyIndicator = TrueFalseEnum.parse(readOnly);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
                         context.getElement().getTagName(), "readOnly");
      e = e.supplementMessage(message);
      throw e;
    }

    TrueFalseEnum writeOnlyIndicator;
    try {
      writeOnlyIndicator = TrueFalseEnum.parse(writeOnly);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
                         context.getElement().getTagName(), "writeOnly");
      e = e.supplementMessage(message);
      throw e;
    }

    if (readOnlyIndicator.booleanValue() && writeOnlyIndicator.booleanValue()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"readOnly, writeOnly");
      throw new ServingXmlException(message);
    }

    boolean reading = writeOnlyIndicator.booleanValue() ? false : true;
    boolean writing = readOnlyIndicator.booleanValue() ? false : true;

    RecordDelimiterFactory delimiterFactory = new RecordDelimiterFactory(separatorFactory, reading, writing);

    return delimiterFactory;
  }
}

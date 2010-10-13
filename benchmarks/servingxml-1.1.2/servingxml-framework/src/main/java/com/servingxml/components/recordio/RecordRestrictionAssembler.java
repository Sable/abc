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

package com.servingxml.components.recordio;

import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.NamePath;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.common.TrueFalseEnum;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordRestrictionAssembler {
  
  private Name recordTypeName = Name.EMPTY;
  private XsltConfiguration xsltConfiguration;
  private String testExpr = "";
  private String negate = TrueFalseEnum.FALSE.toString();
  
  public void setRecordType(Name recordType) {
    this.recordTypeName = recordType;
  }

  public void setTest(String testExpr) {
    this.testExpr = testExpr;
  }

  public void setNegate(String negate) {
    this.negate = negate;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public Restriction assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (recordTypeName.isEmpty() && testExpr.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"recordType or test");
      throw new ServingXmlException(message);
    }

    TrueFalseEnum negateIndicator;
    try {
      negateIndicator = TrueFalseEnum.parse(negate);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "negate");
      e = e.supplementMessage(message);
      throw e;
    }

    RecordAccepterFactory accepterFactory;
    if (testExpr.length() > 0) {
      accepterFactory = RecordAccepterFactory.newInstance(context.getQnameContext(), xsltConfiguration, testExpr);
    } else {
      accepterFactory = RecordAccepterFactory.newInstance(recordTypeName);
    }
    RecordAccepter accepter = accepterFactory.createRecordAccepter();

    Restriction criteria = new RecordRestriction(accepter);
    if (negateIndicator.booleanValue()) {
      criteria = new NegateRestriction(criteria);
    }

    return criteria;
  }
}


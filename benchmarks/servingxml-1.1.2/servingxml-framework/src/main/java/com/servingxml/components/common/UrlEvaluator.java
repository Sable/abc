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

package com.servingxml.components.common;

import java.net.URL;
import java.net.URLEncoder;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.UrlHelper;
import com.servingxml.util.QnameContext;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * The <code>UrlEvaluator</code> implements a class that
 * does parameter substitution in Urls.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                          
public abstract class UrlEvaluator {
                                                       
  public static UrlEvaluator parse(QnameContext context, String s) {
    String base = context.getBase();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,s);
    UrlEvaluator v;
    if (subExpr.isLiteral()) {
      String href = subExpr.evaluateAsString(Record.EMPTY,Record.EMPTY);
      if (href == null || href.length() == 0) {
        throw new ServingXmlException("Unable to resolve URL");
      }
      v = new SimpleUrlEvaluator(base,href);
    } else {
      v = new VariableUrlValue(base,subExpr);
    }
    return v;
  }
  
  public abstract URL evaluateUrl(Record parameters,Record contextRecord);

  public abstract boolean isStringLiteral();
}

class VariableUrlValue extends UrlEvaluator {
  private final String base;
  private final SubstitutionExpr subExpr;
  
  public VariableUrlValue(String base, SubstitutionExpr subExpr) {
    this.base = base;
    this.subExpr = subExpr;
  }
  
  public URL evaluateUrl(Record parameters, Record contextRecord) {
    String href = subExpr.evaluateAsString(parameters,contextRecord);
    if (href == null || href.length() == 0) {
      throw new ServingXmlException("Unable to resolve URL");
    }

    URL fileUrl = UrlHelper.createUrl(href, base);
    return fileUrl;
  }
  
  public boolean isStringLiteral() {
    return false;
  }
}


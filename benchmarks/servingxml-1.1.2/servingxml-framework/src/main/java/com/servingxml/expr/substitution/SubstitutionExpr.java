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

package com.servingxml.expr.substitution;        

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.QnameContext;

public abstract class SubstitutionExpr {
  public final static SubstitutionExpr EMPTY = new LiteralSubstitutionExpr("");
  public static final SubstitutionExpr NULL = new NullSubstitutionExpr();

  public static SubstitutionExpr parseString(QnameContext context, String s) {
    SubstitutionExpr expr = SubstitutionExprParser.parse(context,s);
    return expr;
  } 
  public static SubstitutionExpr parseString(QnameContext context, String input, 
                                             EscapeSubstitutionVariables escapeVariables) {
    SubstitutionExpr expr = SubstitutionExprParser.parse(context,input,escapeVariables);
    return expr;
  } 

  public abstract String evaluateAsString(Record parameters, Record record);

  public abstract String[] evaluateAsStringArray(Record parameters, Record record);

  public abstract boolean isNull();

  public abstract boolean isLiteral();

  static class NullSubstitutionExpr extends SubstitutionExpr {

    public String evaluateAsString(Record parameters, Record record) {
      //System.out.println(getClass().getName()+".intValue MAX_VALUE");
      return "";
    }

    public String[] evaluateAsStringArray(Record parameters, Record record) {
      //System.out.println(getClass().getName()+".intValue MAX_VALUE");
      return new String[0];
    }

    public final boolean isNull() {
      return true;
    }

    public final boolean isLiteral() {
      return false;
    }
  }
}



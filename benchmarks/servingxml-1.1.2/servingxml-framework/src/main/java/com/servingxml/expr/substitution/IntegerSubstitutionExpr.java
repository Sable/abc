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

public abstract class IntegerSubstitutionExpr {
  public static final IntegerSubstitutionExpr MAX_INTEGER = new LiteralIntegerSubstitutionExpr(Integer.MAX_VALUE);
  public static final IntegerSubstitutionExpr ONE = new LiteralIntegerSubstitutionExpr(1);
  public static final IntegerSubstitutionExpr ZERO = new LiteralIntegerSubstitutionExpr(0);
  public static final IntegerSubstitutionExpr MINUS_ONE = new LiteralIntegerSubstitutionExpr(-1);
  public static final IntegerSubstitutionExpr NULL = new NullIntegerSubstitutionExpr();

  public static IntegerSubstitutionExpr parseInt(QnameContext context, String s) {
    IntegerSubstitutionExpr e;
    SubstitutionExpr expr = SubstitutionExprParser.parse(context,s);
    if (expr.isLiteral()) {
      String numberStr = expr.evaluateAsString(Record.EMPTY,Record.EMPTY);
      int n = IntegerSubstitutionExpr.parseInt(numberStr);
      e = new LiteralIntegerSubstitutionExpr(n);
    } else {
      e = new VariableIntegerSubstitutionExpr(expr);
    }
    return e;
  }

  public abstract int evaluateAsInt(Record parameters, Record record);

  public abstract boolean isNull();

  public abstract boolean isLiteral();

  public static int parseInt(String s) {
    if (s == null) {
      String message = "Failed parsing null string, expecting number";
      throw new ServingXmlException(message);
    }
    s = s.trim();
    if (s.length() == 0) {
      String message = "Failed parsing empty string, expecting number";
      throw new ServingXmlException(message);
    }

    try {
      int n = Integer.parseInt(s);
      return n;
    } catch (NumberFormatException e) {
      String message = "Unable to parse number " + s + ".  " + e.getMessage();
      throw new ServingXmlException(message, e);
    }
  }

  static class NullIntegerSubstitutionExpr extends IntegerSubstitutionExpr {

    public int evaluateAsInt(Record parameters, Record record) {
      //System.out.println(getClass().getName()+".evaluateAsInt MAX_VALUE");
      throw new ServingXmlException("Cannot evaluate null Substitution Expression");
    }

    public final boolean isNull() {
      return true;
    }

    public final boolean isLiteral() {
      return false;
    }
  }

  static class VariableIntegerSubstitutionExpr extends IntegerSubstitutionExpr {
    private final SubstitutionExpr subExpr;

    public VariableIntegerSubstitutionExpr(SubstitutionExpr subExpr) {
      this.subExpr = subExpr;
    }

    public int evaluateAsInt(Record parameters, Record record) {

      String s = subExpr.evaluateAsString(parameters, record);
      int n = parseInt(s);
      return n;
    }

    public final boolean isNull() {
      return false;
    }

    public final boolean isLiteral() {
      return false;
    }
  }

  static final class LiteralIntegerSubstitutionExpr extends IntegerSubstitutionExpr {
    private final int value;

    public LiteralIntegerSubstitutionExpr(int value) {
      this.value = value;
    }

    public final int evaluateAsInt(Record parameters, Record record) {
      return value;
    }

    public final boolean isNull() {
      return false;
    }

    public final boolean isLiteral() {
      return true;
    }
  }
}



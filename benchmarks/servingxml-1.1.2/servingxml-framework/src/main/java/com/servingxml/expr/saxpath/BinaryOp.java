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

package com.servingxml.expr.saxpath;

import com.servingxml.util.record.Record;

public abstract class BinaryOp {
  public static final BinaryOp OR = new OrBinaryOp(0);
  public static final BinaryOp AND = new AndBinaryOp(1);
  public static final BinaryOp EQUAL = new EqualBinaryOp(2);
  public static final BinaryOp NE = new NEBinaryOp(2);
  public static final BinaryOp LT = new LTBinaryOp(2);
  public static final BinaryOp LE = new LEBinaryOp(2);
  public static final BinaryOp GT = new GTBinaryOp(2);
  public static final BinaryOp GE = new GEBinaryOp(2);

  private final int opPrecedence;

  protected BinaryOp(int precedence) {
    this.opPrecedence = precedence;
  }

  public abstract Result evaluate(SaxPath path, Record parameters,
                          PredicateTerm lhs, PredicateTerm rhs);

  public int precedence() {
    return opPrecedence;
  }

  static class OrBinaryOp extends BinaryOp {
    public OrBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);
      return new BooleanResult(lhr.asBoolean() || rhr.asBoolean());
    }
  }

  static class AndBinaryOp extends BinaryOp {
    public AndBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);
      return new BooleanResult(lhr.asBoolean() && rhr.asBoolean());
    }
  }

  static class EqualBinaryOp extends BinaryOp {
    public EqualBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);

      int diff = lhr.compareTo(rhr);
      boolean b = diff == 0;
      return new BooleanResult(b);
    }
  }

  static class NEBinaryOp extends BinaryOp {
    public NEBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);

      int diff = lhr.compareTo(rhr);
      boolean b = diff != 0;
      return new BooleanResult(b);
    }
  }

  static class LTBinaryOp extends BinaryOp {
    public LTBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);

      int diff = lhr.compareTo(rhr);
      boolean b = diff < 0;
      return new BooleanResult(b);
    }
  }

  static class LEBinaryOp extends BinaryOp {
    public LEBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);

      int diff = lhr.compareTo(rhr);
      boolean b = diff <= 0;
      return new BooleanResult(b);
    }
  }                                     

  static class GTBinaryOp extends BinaryOp {
    public GTBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                           PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);

      int diff = lhr.compareTo(rhr);
      boolean b = diff > 0;

      return new BooleanResult(b);
    }
  }

  static class GEBinaryOp extends BinaryOp {
    public GEBinaryOp(int precedence) {
      super(precedence);
    }

    public Result evaluate(SaxPath path, Record parameters,
                            PredicateTerm lhs, PredicateTerm rhs) {
      Result lhr = lhs.evaluate(path,parameters);
      Result rhr = rhs.evaluate(path,parameters);

      int diff = lhr.compareTo(rhr);
      boolean b = diff >= 0;
      return new BooleanResult(b);
    }
  }
}


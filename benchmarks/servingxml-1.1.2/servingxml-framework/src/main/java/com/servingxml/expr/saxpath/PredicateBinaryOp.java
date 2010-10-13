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

public class PredicateBinaryOp extends PredicateTerm {
  private final BinaryOp binaryOp;
  private final PredicateTerm lhs;     
  private final PredicateTerm rhs;

  public PredicateBinaryOp(BinaryOp binaryOp, PredicateTerm lhs, PredicateTerm rhs) {
    this.binaryOp = binaryOp;

    this.lhs = lhs;
    this.rhs = rhs;
  }                                                          

  public Result evaluate(SaxPath path, Record parameters) {
    return binaryOp.evaluate(path,parameters,lhs,rhs);
  }

  public int precedence() {
    return binaryOp.precedence();
  }

  public boolean isBinaryOperator() {
    return true;
  }
}


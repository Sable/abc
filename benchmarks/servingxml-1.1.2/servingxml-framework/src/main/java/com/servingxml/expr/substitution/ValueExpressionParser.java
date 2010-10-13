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

import com.servingxml.expr.ExpressionException;
import com.servingxml.expr.Scanner;
import com.servingxml.expr.TokenType;
import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;

/**
 * The <code>ValueExpressionParser</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                             
public class ValueExpressionParser {
  private final QnameContext context;
  private Scanner scanner;

  public ValueExpressionParser(QnameContext context, String path) {
    this.context = context;
    this.scanner = new Scanner(path);
  }
                                                
  public ValueExpression parse() 
  throws ExpressionException {
    scanner.first();

    ValueExpression expression = parseExpression();
    if (scanner.currentTokenType != TokenType.EOF) {
      throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }

    return expression;
  }

  ValueExpression parseExpression() throws ExpressionException {

    ValueExpression expr = null;

    boolean done = false;
    switch (scanner.currentTokenType) {
    case TokenType.DOLLAR:
      nextToken();
      expr = parseParameter();
      break;
    case TokenType.NAME:
      expr = parseField();
      break;
    case TokenType.EOF:
      done = true;
      break;
    default:
      throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }
    return expr;
  }

  ValueExpression parseParameter() throws ExpressionException {
    ValueExpression expr = null;
    if (scanner.currentTokenType == TokenType.NAME) {
      Name name = context.createName(scanner.currentTokenValue);
      expr = new ParameterValueExpression(name);
    } else {
      throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }
    nextToken();
    if (scanner.currentTokenType == TokenType.LEFT_SQUARE) {
      expr = parseIndex(expr);
    }
      
    return expr;
  }

  ValueExpression parseIndex(ValueExpression expr) throws ExpressionException {
    if (scanner.currentTokenType == TokenType.LEFT_SQUARE) {
      nextToken();
      if (scanner.currentTokenType != TokenType.NUMBER) {
        throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
      }
      String s = scanner.currentTokenValue;
      int value = Integer.parseInt(s);
      expr = new IndexedValueExpression(expr,value-1);
      nextToken();
      if (scanner.currentTokenType != TokenType.RIGHT_SQUARE) {
        throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
      }
      nextToken();
    }

    return expr;
  }

  ValueExpression parseField() throws ExpressionException {
    ValueExpression expr = null;
    if (scanner.currentTokenType == TokenType.NAME) {
      Name name = context.createName(scanner.currentTokenValue);
      expr = new FieldValueExpression(name);
    } else {
      throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }
    nextToken();
    if (scanner.currentTokenType == TokenType.LEFT_SQUARE) {
      expr = parseIndex(expr);
    }
    return expr;
  }

  void nextToken() throws ExpressionException {
    scanner.next();
  }
}

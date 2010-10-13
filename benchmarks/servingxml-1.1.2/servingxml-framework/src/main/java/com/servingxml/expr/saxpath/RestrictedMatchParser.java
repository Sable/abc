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

import com.servingxml.util.QnameContext;
import com.servingxml.util.Name;
import com.servingxml.expr.Scanner;
import com.servingxml.expr.TokenType;
import com.servingxml.expr.ExpressionException;

public class RestrictedMatchParser {
  private final QnameContext context;
  private Scanner scanner;

  public RestrictedMatchParser(QnameContext context, String path) {
    this.context = context;
    this.scanner = new Scanner(path);
  }

  public RestrictedMatchPattern parse() 
  throws ExpressionException {
    scanner.first();

    RestrictedMatchPattern expression = parseExpression();
    if (scanner.currentTokenType != TokenType.EOF) {
      throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }

    return expression;
  }

  RestrictedMatchPattern parseExpression() throws ExpressionException {

    RestrictedMatchPattern expr = null;

    switch (scanner.currentTokenType) {
      case TokenType.SLASH:
        nextToken();
        RelativePath rel = parseRelativePath();
        expr = new AbsolutePath(rel);
        break;
      case TokenType.NAME:
        expr = parseRelativePath();
        break;
      default:
        throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }
    return expr;
  }

  RelativePath parseRelativePath() throws ExpressionException {

    PathEntry tail = null;

    boolean done = false;
    while (!done) {
      if (scanner.currentTokenType == TokenType.NAME) {
        String qname = scanner.currentTokenValue;
        Name name = context.createName(qname);
        //System.out.println(getClass().getName()+".parseRelativePath qname="+qname+", name="+name);
        int nameSymbol = context.getNameTable().lookupSymbol(name);
        tail = new NamePathEntry(nameSymbol,tail);
      } else if (scanner.currentTokenType == TokenType.SLASH) {
        tail = new SlashPathEntry(tail);
      } else if (scanner.currentTokenType == TokenType.STAR) {
        tail = new WildcardPathEntry(tail);
      }
      if (scanner.currentTokenType != TokenType.SLASH) {
        nextToken();
      }
      if (scanner.currentTokenType == TokenType.SLASH) {
        nextToken();
      } else if (scanner.currentTokenType == TokenType.LEFT_SQUARE) {
        nextToken();
        Predicate predicate = parsePredicate();
        tail = new PredicatePathEntry(predicate,tail);
        if (scanner.nextTokenType != TokenType.RIGHT_SQUARE) {
          throw new ExpressionException("']' expected but found " + scanner.currentTokenType);
        }
        nextToken();
      } else {
        done = true;
      }
    } 

    RelativePath expr = new RelativePath(tail);

    return expr;
  }

  void nextToken() throws ExpressionException {
    scanner.next();
  }

  PredicateTerm parsePredicate() throws ExpressionException {
    return parseExpression(parsePrimary(),0);
  }

  PredicateTerm parseExpression(PredicateTerm lhs, int minPrecedence) 
  throws ExpressionException {
    //System.out.println("parseExpression minPrecedence = " + minPrecedence + " " + TokenType.getLabel(scanner.nextTokenType));
    boolean done = false;
    while (!done 
           && scanner.nextTokenType != TokenType.RIGHT_PARENTHESIS 
           && scanner.nextTokenType != TokenType.RIGHT_SQUARE) {
      BinaryOp op = parseOperator(scanner.nextTokenType,scanner.nextTokenValue);
      if (op.precedence() < minPrecedence) {
        done = true;
      } else {
        nextToken();  // op = nextToken
        nextToken();  // primary = nextToken
        PredicateTerm rhs = parsePrimary();
        boolean done2 = false;
        while (!done2
               && scanner.nextTokenType != TokenType.RIGHT_PARENTHESIS 
               && scanner.nextTokenType != TokenType.RIGHT_SQUARE) {
          BinaryOp op2 = parseOperator(scanner.nextTokenType,scanner.nextTokenValue);
          if (op2.precedence() <= op.precedence()) {
            done2 = true;
          } else {
            rhs = parseExpression(rhs,op2.precedence());
            //System.out.println("expr parsed " + minPrecedence);
          }
        }
        lhs = new PredicateBinaryOp(op,lhs,rhs);
      }
    }
    return lhs;
  }

  BinaryOp parseOperator(int tokenType, String tokenValue) 
  throws ExpressionException {

    BinaryOp op;
    //System.out.println("parseOperator " + TokenType.getLabel(tokenType) + ", " + tokenValue);
    switch (tokenType) {
      case TokenType.NAME:                      
        {
          if (tokenValue == "or") {
            op = BinaryOp.OR;
          } else if (tokenValue == "and") {
            op = BinaryOp.AND;
          } else {
            throw new ExpressionException("Unrecognized symbol " + tokenValue);
          }
        }
        break;
      case TokenType.EQUAL:
        op = BinaryOp.EQUAL;
        break;
      case TokenType.NE:
        op = BinaryOp.NE;
        break;
      case TokenType.LT:
        op = BinaryOp.LT;
        break;
      case TokenType.LE:
        op = BinaryOp.LE;
        break;
      case TokenType.GT:
        op = BinaryOp.GT;
        break;
      case TokenType.GE:
        op = BinaryOp.GE;
        break;
      default:
        throw new ExpressionException("Unexpected operator " + TokenType.getLabel(scanner.currentTokenType));
    }
    return op;
  }

  PredicateTerm parsePrimary() throws ExpressionException {
    //System.out.println("parsePrimary " + scanner.currentTokenValue + ", " + TokenType.getLabel(scanner.currentTokenType));

    PredicateTerm term = null;
    switch (scanner.currentTokenType) {
      case TokenType.AT:
        {
          nextToken();
          if (scanner.currentTokenType != TokenType.NAME) {
            throw new ExpressionException("Attribute name expected.");
          }
          String qname = scanner.currentTokenValue;
          Name attributeName = context.createName(qname);
          term = new PredicateAttribute(attributeName);
        }
        break;                                                       
      case TokenType.DOLLAR:
        {
          nextToken();
          if (scanner.currentTokenType != TokenType.NAME) {
            throw new ExpressionException("Parameter name expected.");
          }
          String qname = scanner.currentTokenValue;
          Name parameterName = context.createName(qname);
          term = new PredicateParameter(parameterName);
        }
        break;
      case TokenType.STRING_LITERAL:
        {
          term = new PredicateStringLiteral(scanner.currentTokenValue);
        }
        break;
      case TokenType.NUMBER:
        {
          try {
            double x = Double.parseDouble(scanner.currentTokenValue);
            term = new PredicateNumber(x);
          } catch (NumberFormatException e) {
            String message = "Invalid double value \"" + scanner.currentTokenValue + "\"";
            throw new ExpressionException(message);
          }
        }
        break;

      case TokenType.LEFT_PARENTHESIS:
        {
          nextToken();                                     
          term = parsePredicate();
          nextToken();                                     
        }
        break;

      case TokenType.EOF:
        throw new ExpressionException("Expression terminated unexpectedly.");
      default:
        throw new ExpressionException("Unexpected token " + TokenType.getLabel(scanner.currentTokenType));
    }

    return term;
  }
}

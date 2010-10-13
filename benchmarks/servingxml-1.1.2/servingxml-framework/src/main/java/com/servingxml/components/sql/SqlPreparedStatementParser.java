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

package com.servingxml.components.sql;

import java.util.ArrayList;
import java.util.List;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.QnameContext;
import com.servingxml.expr.ExpressionException;
import com.servingxml.expr.substitution.ValueExpression;
import com.servingxml.expr.substitution.ValueExpressionParser;
import com.servingxml.components.quotesymbol.QuoteSymbol;

/**
 * The <code>SqlPreparedStatementParser</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                             
public class SqlPreparedStatementParser {

  public static SqlPreparedStatement parse(QnameContext context, String input, QuoteSymbol quoteSymbol) {

    try {
      int begin = 0;
      int quoteState = 0;
      int expressionState = 0;
      char[] escapeSequence = quoteSymbol.getEscapeSequence().toCharArray();

      List<ValueExpression> argList = new ArrayList<ValueExpression>();
      StringBuilder strBuffer = new StringBuilder();

      for (int pos = 0; pos < input.length(); ++pos) {
        char ch = input.charAt(pos);
        if (expressionState == 0) {
          if (ch == '{') {
            expressionState = 1;
            if (pos > begin) {
              String ls = input.substring(begin,pos);
              strBuffer.append(ls);
            }
            begin = pos + 1;
          } else if (quoteState == 1) {
            boolean escape = false;
            if (ch == escapeSequence[0]) {
              if ((pos + escapeSequence.length) <= input.length()) {
                escape = true;
                for (int j = 1; escape && j < escapeSequence.length; ++j) {
                  char ch2 = input.charAt(pos+j);
                  if (ch2 != escapeSequence[j]) {
                    escape = false;
                  }
                }
              }
            }
            if (!escape) {
              if (ch == quoteSymbol.getCharacter()) {
                quoteState = 0;
              }
            } else {
              pos += escapeSequence.length - 1;
            }
          } else {
            if (ch == quoteSymbol.getCharacter()) {
              quoteState = 1;
            }
          }
        } else if (expressionState == 1) {
          if (ch == '}') {
            expressionState = 0;
            String s = input.substring(begin,pos);
            ValueExpressionParser parser = new ValueExpressionParser(context,s);
            ValueExpression valueExpr = parser.parse();
            strBuffer.append("?");
            argList.add(valueExpr);
            begin = pos + 1;
          }
        }
      }

      if (begin < input.length()) {
        String subs = input.substring(begin);
        strBuffer.append(subs);
      }

      ValueExpression[] args = new ValueExpression[argList.size()];
      args = argList.toArray(args);

      return new SqlPreparedStatement(strBuffer.toString(), args);
    } catch (ExpressionException e) {
      String message = "Expression " + input + " not recognized. " + e.getMessage() + ".";
      throw new ServingXmlException(message);
    }
  }
}

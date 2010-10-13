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

import java.util.ArrayList;
import java.util.List;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.QnameContext;
import com.servingxml.expr.ExpressionException;

/**
 * The <code>SubstitutionExpr</code> implements a class that
 * does parameter substitution in strings.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                             
public class SubstitutionExprParser {
                                                       
  public static SubstitutionExpr parse(QnameContext context, String input) {
    
    List<SubstitutionExpr> valueList = new ArrayList<SubstitutionExpr>();

    try {
      boolean startExpr = false;
      char prev = ' ';
      int begin = 0;
      for (int i = 0; i < input.length(); ++i) {
        char c = input.charAt(i);
        if (!startExpr) {
          if (prev == '{') {
            if ((i-1) > begin) {
              String ls = input.substring(begin,i-1);
              valueList.add(new LiteralSubstitutionExpr(ls));
            }
            begin = i;
            startExpr = true;
          }
        } else if (c == '}') {
          String s = input.substring(begin,i);
          SubstitutionParser parser = new SubstitutionParser(context,s);
          SubstitutionExpr subExpr = parser.parse();
          valueList.add(subExpr);
          startExpr = false;
          begin = i + 1;
        }
        prev = c;
      }

      if (begin < input.length()) {
        String subs = input.substring(begin);
        valueList.add(new LiteralSubstitutionExpr(subs));
      }
    } catch (ExpressionException e) {
      String message = "Expression " + input + " not recognized. " + e.getMessage() + ".";
      throw new ServingXmlException(message);
    }

    
    SubstitutionExpr v;
    if (valueList.size() == 0) {
      v = new LiteralSubstitutionExpr("");
    } else if (valueList.size() == 1) {
      v = valueList.get(0);
    } else {
      SubstitutionExpr[] values = new SubstitutionExpr[valueList.size()];
      values = valueList.toArray(values);
      v = new MultiPartSubstitutionExpr(values);
    }
    
    return v;
  }

  public static SubstitutionExpr parse(QnameContext context, String input, 
                                       EscapeSubstitutionVariables escapeVariables) {
    SubstitutionExpr expr;
    if (!escapeVariables.doEscape()) {
      expr = parse(context,input);
    } else {
      expr = parse2(context,input,escapeVariables);
    }
    return expr;
  }

  private static SubstitutionExpr parse2(QnameContext context, String input, 
                                       EscapeSubstitutionVariables escapeVariables) {

    List<SubstitutionExpr> valueList = new ArrayList<SubstitutionExpr>();

    try {
      int begin = 0;
      int quoteState = 0;
      int expressionState = 0;
      char[] escapeSequence = escapeVariables.getEscapeSequence().toCharArray();

      for (int pos = 0; pos < input.length(); ++pos) {
        char ch = input.charAt(pos);
        if (expressionState == 0) {
          if (ch == '{') {
            expressionState = 1;
            if (pos > begin) {
              String ls = input.substring(begin,pos);
              valueList.add(new LiteralSubstitutionExpr(ls));
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
              if (ch == escapeVariables.getCharacter()) {
                quoteState = 0;
              }
            } else {
              pos += escapeSequence.length - 1;
            }
          } else {
            if (ch == escapeVariables.getCharacter()) {
              quoteState = 1;
            }
          }
        } else if (expressionState == 1) {
          if (ch == '}') {
            expressionState = 0;
            String s = input.substring(begin,pos);
            SubstitutionParser parser = new SubstitutionParser(context,s);
            SubstitutionExpr subExpr = parser.parse();
            if (quoteState == 1) {
              subExpr = new EscapedSubstitutionExpression(subExpr,escapeVariables);
            }
            valueList.add(subExpr);
            begin = pos + 1;
          }
        }
      }

      if (begin < input.length()) {
        String subs = input.substring(begin);
        valueList.add(new LiteralSubstitutionExpr(subs));
      }
    } catch (ExpressionException e) {
      String message = "Expression " + input + " not recognized. " + e.getMessage() + ".";
      throw new ServingXmlException(message);
    }


    SubstitutionExpr v;
    if (valueList.size() == 0) {
      v = SubstitutionExpr.EMPTY;
    } else if (valueList.size() == 1) {
      v = valueList.get(0);
    } else {
      SubstitutionExpr[] values = new SubstitutionExpr[valueList.size()];
      values = valueList.toArray(values);
      v = new MultiPartSubstitutionExpr(values);
    }

    return v;
  }
}

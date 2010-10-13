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

package com.servingxml.expr;

public class TokenType {
  public static final int UNKNOWN = -1;
  public static final int EOF = 0;
  public static final int SLASH = 1;
  public static final int DOT = 2;
  public static final int AT = 3;
  public static final int DOLLAR = 4;
  public static final int NAME = 5;
  public static final int EQUAL = 6;
  public static final int NE = 7;
  public static final int LT = 8;
  public static final int LE = 9;
  public static final int GT = 10;
  public static final int GE = 11;
  public static final int COLON = 12;
  public static final int LEFT_SQUARE = 13;
  public static final int RIGHT_SQUARE = 14;
  public static final int NUMBER = 15;
  public static final int STRING_LITERAL = 16;
  public static final int LEFT_PARENTHESIS = 17;
  public static final int RIGHT_PARENTHESIS = 18;
  public static final int STAR = 19;

  private static String[] labels = new String[100];
  static {
    labels[EOF] = "<eof>";
    labels[SLASH] = "/";
    labels[DOT] = ".";
    labels[AT] = "@";
    labels[DOLLAR] = "$";
    labels[NAME] = "<name>";
    labels[EQUAL] = "=";
    labels[NE] = "!=";
    labels[LT] = "<";
    labels[LE] = "<=";
    labels[GT] = ">";
    labels[GE] = ">=";
    labels[COLON] = ":";
    labels[LEFT_SQUARE] = "[";
    labels[RIGHT_SQUARE] = "]";
    labels[NUMBER] = "<number>";
    labels[STRING_LITERAL] = "<string>";
    labels[LEFT_PARENTHESIS] = "(";
    labels[RIGHT_PARENTHESIS] = ")";
    labels[STAR] = "*";
  }

  public static String getLabel(int tokenType) {
    return tokenType >= 0 && tokenType < 100 ? labels[tokenType] : "<unknown>";
  }

}



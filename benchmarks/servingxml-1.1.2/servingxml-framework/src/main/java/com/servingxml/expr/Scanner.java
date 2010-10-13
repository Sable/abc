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

public class Scanner {
  private final char[] buffer;

  private int bufferOffset = 0;

  public int nextTokenType = TokenType.EOF;
  private int nextTokenStartOffset = 0;
  private int prevTokenType = TokenType.UNKNOWN;
  public String nextTokenValue = "";

  public int currentTokenType = TokenType.EOF;
  public String currentTokenValue = "";

  private int currentTokenStartOffset = 0;

  public Scanner(String input) {
    this.buffer = input.toCharArray();
  }

  public String getInput() {
    return new String(buffer);
  }

  public void first() 
  throws ExpressionException {
    this.nextTokenType = TokenType.EOF;
    nextTokenValue = "";
    this.nextTokenStartOffset = 0;
    this.bufferOffset = 0;

    lookAhead();
    next();
  }

  public void next() throws ExpressionException {
    prevTokenType = currentTokenType;
    currentTokenType = nextTokenType;
    currentTokenValue = nextTokenValue;
    currentTokenStartOffset = nextTokenStartOffset;

    lookAhead();
  }                 

  private void lookAhead() throws ExpressionException {
    prevTokenType = nextTokenType;
    nextTokenValue = "";
    nextTokenStartOffset = bufferOffset;

    boolean done = false;
    while (!done) {
      if (bufferOffset >= buffer.length) {
        nextTokenType = TokenType.EOF;
        done = true;
      } else {
        char c = buffer[bufferOffset++];

        switch (c) {
          case '@':
            nextTokenType = TokenType.AT;
            done = true;
            break;
          case '=':
            nextTokenType = TokenType.EQUAL;
            done = true;
            break;
          case '<':
            if (bufferOffset < (buffer.length-1) && buffer[bufferOffset] == '=') {
              bufferOffset++;
              nextTokenType = TokenType.LE;
            } else {
              nextTokenType = TokenType.LT;
            }
            done = true;
            break;
          case '>':
            if (bufferOffset < (buffer.length-1) && buffer[bufferOffset] == '=') {
              bufferOffset++;
              nextTokenType = TokenType.GE;
            } else {
              nextTokenType = TokenType.GT;
            }
            done = true;
            break;
          case '!':
            if (bufferOffset >= (buffer.length-1) || buffer[bufferOffset] != '=') {
              throw new ExpressionException("! not followed by =");
            }
            bufferOffset++;
            nextTokenType = TokenType.NE;
            done = true;
            break;
          case '"':
          case '\'':
            StringBuilder buf = new StringBuilder();
            boolean found = false;
            for (; !found && bufferOffset < buffer.length; ++bufferOffset) {
              if (buffer[bufferOffset] == c) {
                found = true;
              } else {
                buf.append(buffer[bufferOffset]);
              }
            }
            if (!found) {
              throw new ExpressionException("Unmatched quote");
            }
            nextTokenValue = buf.toString();
            nextTokenType = TokenType.STRING_LITERAL;
            done = true;
            break;
          case '/':
            nextTokenType = TokenType.SLASH;
            done = true;      
            break;
          case '.':
            nextTokenType = TokenType.DOT;
            done = true;      
            break;
          case '*':
            nextTokenType = TokenType.STAR;
            done = true;      
            break;
          case '$':
            nextTokenType = TokenType.DOLLAR;
            done = true;      
            break;
          case '[':
            nextTokenType = TokenType.LEFT_SQUARE;
            done = true;      
            break;
          case ']':
            nextTokenType = TokenType.RIGHT_SQUARE;
            done = true;      
            break;
          case '(':
            nextTokenType = TokenType.LEFT_PARENTHESIS;
            done = true;      
            break;
          case ')':
            nextTokenType = TokenType.RIGHT_PARENTHESIS;
            done = true;      
            break;
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            while (!done && bufferOffset < buffer.length) {
              c = buffer[bufferOffset];
              if (!(Character.isDigit(c) || c == '.')) {
                done = true;
              } else {
                bufferOffset++;
              }
            }
            nextTokenValue = new String(buffer, nextTokenStartOffset, bufferOffset-nextTokenStartOffset);
            nextTokenType = TokenType.NUMBER;
            done = true;
            break;
          case '\n':
          case ' ':
          case '\t':
          case '\r':
            nextTokenStartOffset = bufferOffset;
            break;
          default:
            if (c < 0x80 && !Character.isLetter(c)) {
              throw new ExpressionException("Invalid character '" + c + "' in expression");
            }
            while (!done && bufferOffset < buffer.length) {
              c = buffer[bufferOffset];
              switch (c) {
                case ':':
                  bufferOffset++;
                  break;
                case '.':
                case '-':                             
                case '_':
                  bufferOffset++;
                  break;
                default:
                  if (c < 0x80 && !Character.isLetterOrDigit(c)) {
                    done = true;
                  } else {
                    bufferOffset++;
                  }
              }
            }
            //New
            if (bufferOffset > nextTokenStartOffset) {
              nextTokenValue = new String(buffer, nextTokenStartOffset, bufferOffset-nextTokenStartOffset).intern();
              nextTokenType = TokenType.NAME;
            } else {
              nextTokenValue = "";
              nextTokenType = TokenType.EOF;
            }
            done = true;
        }
      }
    }
  } 
}

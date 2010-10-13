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

package com.servingxml.components.flatfile.parsing;

import com.servingxml.components.flatfile.options.CharTrimmer;
import com.servingxml.components.flatfile.options.CharDelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.QuoteSymbolCharChecker;
import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.ServingXmlException;

public class DelimitedFieldCharTokenizer extends CharTokenizer {
  private final QuoteSymbolCharChecker quoteSymbolChecker;
  private final CharDelimiterExtractor[] recordCharDelimiterExtractors;
  private final CharDelimiterExtractor[] fieldCharDelimiterExtractors;
  private final boolean omitFinalFieldDelimiter;
  private final boolean trimLeading;
  private final boolean trimTrailing;
  private final CharTrimmer charTrimmer;
  private final int maxLength;

  private char[] input;
  private int inputOffset = 0;
  private int inputLength = 0;

  private int currentToken = Token.EOF;
  private String currentTokenValue;
  private int nextToken = Token.NIL;
  private String nextTokenValue = null;
  private int nextTokenStartOffset = 0;
  private final CharArrayBuilder charArrayBuilder;

  public DelimitedFieldCharTokenizer(FlatFileOptions flatFileOptions, int maxLength) {
    this.quoteSymbolChecker = flatFileOptions.getQuoteSymbolCharChecker();
    this.fieldCharDelimiterExtractors = flatFileOptions.getFieldCharDelimiterExtractors();
    this.recordCharDelimiterExtractors = flatFileOptions.getRecordCharDelimiterExtractors();
    this.omitFinalFieldDelimiter = flatFileOptions.isOmitFinalFieldDelimiter();
    this.trimLeading = flatFileOptions.isTrimLeading();
    this.trimTrailing = flatFileOptions.isTrimTrailing();
    this.charTrimmer = flatFileOptions.getCharTrimmer();
    this.maxLength = maxLength;
    this.charArrayBuilder = new CharArrayBuilder();
  }

  public int getInputOffset() {
    return inputOffset;
  }

  public int getCurrentToken() {
    return currentToken;
  }

  public String getCurrentTokenValue() {
    return currentTokenValue;
  }

  public void tokenize(char[] input, int start, int length) {
    this.input = input;
    this.inputOffset = start;
    this.inputLength = length;
    next();
  }

  public void next() {
    if (nextToken != Token.NIL) {
      currentToken = nextToken;
      currentTokenValue = nextTokenValue;
      nextToken = Token.NIL;
      nextTokenValue = null;
    } else {
      analyze();
    }
    if (currentTokenValue==null) {
      currentTokenValue="";
    }
  }

  public void analyze() {
    try {
      nextToken = Token.NIL;
      nextTokenValue = null;
      currentTokenValue = null;
      boolean done = false;
      boolean inQuotes = false;
      charArrayBuilder.clear();
      int maxPosition = (maxLength >= 0 && maxLength <= inputLength - inputOffset) ? maxLength+inputOffset : inputLength;

      //  Initialize leadingCount and leftLimitTrailingSpace
      int leadingCount = 0;
      if (trimLeading) {
        leadingCount = charTrimmer.countLeadingWhitespace(input, inputOffset, maxPosition-inputOffset);
      }
      int leftLimitTrailingSpace = leadingCount;

      while (!done) {
        int n = 0;
        if (inputOffset >= maxPosition) {
          if (inputOffset >= inputLength) {
            if (charArrayBuilder.length() == 0) {
              currentToken = Token.EOF;
            }
            nextToken = Token.EOF;
          } else {
            nextToken = Token.END_OF_FIELD;
          }
          //System.out.println(getClass().getName()+".analyze inputOffset >= maxPosition, inputOffset = " + inputOffset +", inputLength = " + inputLength);
          done = true;
        } else {
          if (inQuotes) {
            n = quoteSymbolChecker.readEscapedQuoteSymbol(input, inputOffset, maxPosition-inputOffset, charArrayBuilder);
          }
          if (n > 0) {
            inputOffset += n;
          } else {
            n = quoteSymbolChecker.foundQuoteSymbol(input, inputOffset, maxPosition-inputOffset);
            if (n > 0) {
              //System.out.println("readQuotedField-foundQuoteSymbol " + new String(input,inputOffset+inputOffset,length-inputOffset));
              inQuotes = !inQuotes;
              inputOffset += n;
            } else if (inQuotes) {
              charArrayBuilder.append(input[inputOffset]);
              //System.out.println("Appending " + new String(input,inputOffset+inputOffset,1) + ", buflen=" + charArrayBuilder.length());
              ++inputOffset;
              leftLimitTrailingSpace = charArrayBuilder.length();
            } else {
              //System.out.println(getClass().getName()+".analyze before check delimiter, inputOffset = " + inputOffset +", maxPosition = " + maxPosition);

              boolean fieldDelimiterEscaped = false;
              for (int i = 0; nextToken != Token.END_OF_FIELD && !fieldDelimiterEscaped && i < fieldCharDelimiterExtractors.length; ++i) {
                int escapedLength = fieldCharDelimiterExtractors[i].readEscapedDelimiter(input, inputOffset, maxPosition-inputOffset, charArrayBuilder);
                if (escapedLength > 0) {
                  inputOffset += escapedLength;
                  fieldDelimiterEscaped = true;
                } else {
                  int delimiterLength = fieldCharDelimiterExtractors[i].foundEndDelimiter(input, inputOffset, maxPosition-inputOffset);
                  if (delimiterLength > 0) {
                    inputOffset += delimiterLength;
                    done = true;
                    nextToken = Token.END_OF_FIELD;
                    //System.out.println(getClass().getName()+".analyze end-of-field found, inputOffset = " + inputOffset +", maxPosition = " + maxPosition);
                  }
                }
              }
              //System.out.println(getClass().getName()+".analyze after check delimiter, nextToken = " + nextToken + ", fieldDelimiterEscaped = " + fieldDelimiterEscaped + ",inputOffset = " + inputOffset +", maxPosition = " + maxPosition);
              if (nextToken != Token.END_OF_FIELD && !fieldDelimiterEscaped) {
                charArrayBuilder.append(input[inputOffset]);
                //System.out.println("Appending " + new String(input,inputOffset,1) + ", buflen=" + charArrayBuilder.length());
                ++inputOffset;
              }
            }
          }
        }
      }
      if (nextToken == Token.END_OF_FIELD || omitFinalFieldDelimiter) {
        currentTokenValue = "";
        currentToken = Token.STRING;
        int len = charArrayBuilder.length() - leadingCount;
        int trailingCount = 0;
        if (trimTrailing) {
          trailingCount = charTrimmer.countTrailingWhitespace(charArrayBuilder.buffer(), leftLimitTrailingSpace, 
                                                              charArrayBuilder.length()-leftLimitTrailingSpace);
          //System.out.println(getClass().getName() + ".analyze "
          //  + "leftLimitTrailingSpace="+leftLimitTrailingSpace 
          //  + ", buflen=" + charArrayBuilder.length()
          //  + ", trailingCount=" + trailingCount
          // + ", leadingCount=" + leadingCount);
        }
        if (len-trailingCount > 0) {
          //value = new String(charArrayBuilder.buffer(), leadingCount, len-trailingCount);
          currentTokenValue = new String(charArrayBuilder.toCharArray(),leadingCount,len-trailingCount);
          //System.out.println(getClass().getName() + ".analyze "
          // + ".readField buffer=" + new String(charArrayBuilder.buffer()) + "." 
          // + ", currentTokenValue=" + currentTokenValue + "." + " leadingCount = " + leadingCount + ", len = " + len + ", trailingCount= " + trailingCount);
        }
      }
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}


  

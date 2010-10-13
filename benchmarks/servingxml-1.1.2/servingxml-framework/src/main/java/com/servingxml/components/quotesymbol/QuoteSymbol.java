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

package com.servingxml.components.quotesymbol;

import java.nio.charset.Charset;

import com.servingxml.components.flatfile.options.QuoteSymbolByteChecker;
import com.servingxml.components.flatfile.options.QuoteSymbolByteCheckerImpl;
import com.servingxml.components.flatfile.options.QuoteSymbolCharChecker;
import com.servingxml.components.flatfile.options.QuoteSymbolCharCheckerImpl;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;

/**
 * 
 * @author  Daniel A. Parker
 */

public class QuoteSymbol {
  private final char character;
  private final String escapeWithSequence;

  public QuoteSymbol(char character, String escapeSequence) {
    this.character = character;
    this.escapeWithSequence = escapeSequence;
  }

  public char getCharacter() {
    return character;
  }

  public String getEscapeSequence() {
    return escapeWithSequence;
  }

  public boolean mustEscape(char ch) {
    return ch == character;
  }

  public String escape(String s) {
    int pos = s.indexOf(character);

    if (pos != -1) {
      StringBuilder buf = new StringBuilder(s.length()+2);
      escape(s, pos, buf);
      s = buf.toString();
    }
    return s;
  }

  public void escape(String input, StringBuilder output) {
    int pos = input.indexOf(character);
    escape(input, pos, output );
  }

  private void escape(String input, int pos, StringBuilder output) {

    if (pos != -1) {
      if (pos > 0) {
        for (int i = 0; i < pos; ++i) {
          char ch = input.charAt(i);
          output.append(ch);
        }
      }
      output.append(escapeWithSequence);
      for (int i = pos+1; i < input.length(); ++i) {
        char ch = input.charAt(i);
        if (ch == character) {
          output.append(escapeWithSequence);
        } else {
          output.append(ch);
        }
      }
    } else {
      output.append(input);
    }
  }

  public QuoteSymbolByteChecker createQuoteSymbolChecker(Charset charset) {
    byte[] rawSymbol = CharsetHelper.charactersToBytes(new char[]{character}, charset);
    byte[] rawEscapeWith = CharsetHelper.stringToBytes(escapeWithSequence, charset);

    return new QuoteSymbolByteCheckerImpl(rawSymbol, rawEscapeWith);
  }

  public QuoteSymbolCharChecker createCharQuoteSymbolChecker() {
    return new QuoteSymbolCharCheckerImpl(new char[]{character}, escapeWithSequence.toCharArray());
  }
}


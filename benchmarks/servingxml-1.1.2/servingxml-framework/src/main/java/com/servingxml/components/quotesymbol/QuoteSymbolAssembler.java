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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.quotesymbol.QuoteSymbol;

/**
 * The <code>QuoteSymbolAssembler</code> implements an assembler for
 * assembling <code>QuoteSymbol</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class QuoteSymbolAssembler {
  
  private char character = '\\';
  private char escapeCharacter = '\\';
  private String escapeSequence = "";

  //  Deprecated
  public void setValue(char character) {
    this.character = character;
  }

  public void setCharacter(char character) {
    this.character = character;
  }
  
  //  Deprecated
  public void setEscapedBy(char escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  //  Deprecated
  public void setEscapeChar(char escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  public void setEscapeCharacter(char escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  //  Deprecated
  public void setEscapeWith(String escapeSequence) {
    this.escapeSequence = escapeSequence;
  }

  public void setEscapeSequence(String escapeSequence) {
    this.escapeSequence = escapeSequence;
  }
                                                       
  public QuoteSymbol assemble(ConfigurationContext context) {

    QuoteSymbol quoteSymbol;

    if (escapeSequence.length() > 0) {
      quoteSymbol = new QuoteSymbol(character, escapeSequence);
    } else {
      quoteSymbol = new QuoteSymbol(character, new String(new char[]{escapeCharacter,character}));
    }

    return quoteSymbol;
  }
}

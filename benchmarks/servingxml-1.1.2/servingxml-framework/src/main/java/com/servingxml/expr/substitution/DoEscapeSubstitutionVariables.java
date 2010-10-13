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

import java.nio.charset.Charset;

import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;

/**
 * 
 * @author  Daniel A. Parker
 */

public class DoEscapeSubstitutionVariables implements EscapeSubstitutionVariables {
  private final char character;
  private final String escapeSequence;

  public DoEscapeSubstitutionVariables(char character, String escapeSequence) {
    this.character = character;
    this.escapeSequence = escapeSequence;
  }

  public DoEscapeSubstitutionVariables(char character) {
    this.character = character;
    this.escapeSequence = new String(new char[]{character, character});
  }

  public char getCharacter() {
    return character;
  }

  public String getEscapeSequence() {
    return escapeSequence;
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
      output.append(escapeSequence);
      for (int i = pos+1; i < input.length(); ++i) {
        char ch = input.charAt(i);
        if (ch == character) {
          output.append(escapeSequence);
        } else {
          output.append(ch);
        }
      }
    } else {
      output.append(input);
    }
  }

  public boolean doEscape() {
    return true;
  }
}


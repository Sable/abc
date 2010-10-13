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

public interface EscapeSubstitutionVariables {
  public static final EscapeSubstitutionVariables DO_NOT_ESCAPE = new DoNotEscapeSubstitutionVariables();

  boolean doEscape();

  char getCharacter();

  String getEscapeSequence();

  boolean mustEscape(char ch);

  String escape(String s);

  void escape(String input, StringBuilder output);

  public final static class DoNotEscapeSubstitutionVariables implements EscapeSubstitutionVariables {
    private final char character = '\\';
    private final String escapeSequence = "\\";

    public DoNotEscapeSubstitutionVariables() {
    }

    public boolean doEscape() {
      return false;
    }

    public final char getCharacter() {
      return character;
    }

    public final String getEscapeSequence() {
      return escapeSequence;
    }

    public final boolean mustEscape(char ch) {
      return false;
    }

    public final String escape(String s) {
      return s;
    }

    public final void escape(String input, StringBuilder output) {
    }

    private final void escape(String input, int pos, StringBuilder output) {
    }
  }
}


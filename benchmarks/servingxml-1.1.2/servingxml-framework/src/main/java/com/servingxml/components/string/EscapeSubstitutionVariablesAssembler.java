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

package com.servingxml.components.string;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.expr.substitution.EscapeSubstitutionVariables;
import com.servingxml.expr.substitution.DoEscapeSubstitutionVariables;

/**
 * The <code>EscapeSubstitutionVariablesAssembler</code> implements an assembler for
 * assembling <code>EscapeSubstitutionVariables</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class EscapeSubstitutionVariablesAssembler {

  private boolean escape = true;
  private char character = '"';
  private char escapeCharacter = '\\';
  private String escapeSequence = "";

  public void setEscape(boolean escape) {
    this.escape = escape;
  }
  
  public void setCharacter(char character) {
    this.character = character;
  }
  
  public void setEscapeCharacter(char escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  public void setEscapedBy(char escapeCharacter) {
    this.escapeCharacter = escapeCharacter;
  }

  public void setEscapeSequence(String escapeSequence) {
    this.escapeSequence = escapeSequence;
  }
                                                       
  public EscapeSubstitutionVariables assemble(ConfigurationContext context) {

    EscapeSubstitutionVariables escapeVariables;

    if (escape) {
      if (escapeSequence.length() > 0) {
        escapeVariables = new DoEscapeSubstitutionVariables(character, escapeSequence);
      } else {
        escapeVariables = new DoEscapeSubstitutionVariables(character, new String(new char[]{escapeCharacter,character}));
      }
    } else {
      escapeVariables = EscapeSubstitutionVariables.DO_NOT_ESCAPE;
    }
    return escapeVariables;
  }
}

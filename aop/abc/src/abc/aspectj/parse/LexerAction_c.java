/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Pavel Avgustinov
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

/*
 * Created on Sep 15, 2004
 */
package abc.aspectj.parse;

/**
 * Default implementation of the LexerAction interface.
 * 
 * This class can be constructed with one or two Integer parameters. The
 * one-argument form calls the two-argument form with null for the second
 * parameter. The first argument is the parser token that should be
 * returned, the second is the state that the lexer should switch to
 * when the corresponding keyword is encountered - no switch is performed
 * if this argument is null. 
 * 
 * @author Pavel Avgustinov
 */
public class LexerAction_c implements LexerAction {
    public Integer token;
    public Integer nextState;

    public LexerAction_c(Integer t) {
        this(t, null);
    }
    
    public LexerAction_c(Integer t, Integer s) {
        token = t;
        nextState = s;
    }
    
    public int getToken(AbcLexer lexer) {
        if(nextState != null) {
            lexer.enterLexerState(nextState.intValue());
        }
        return token.intValue();
    }

}

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
 * Classes implement this interface to indicate they can perform the actions
 * required by the lexer when a particular keyword is scanned.
 * 
 * @author Pavel Avgustinov
 */
public interface LexerAction {
    /**
     * This function is called when the keyword that is associated with this action
     * is encountered in the lexer. In order to be useful, it should probably make
     * use of some of Lexer_c's public members to produce the necessary side-effects.
     * 
     * @param lexer The lexer object that the action should apply to. It is used for
     *          the calls that produce side-effects. 
     *  
     * @return The token that should be passed on to the parser, as defined in
     *      aspectj.ppg or similar files from extensions to abc.
     */
    public int getToken(AbcLexer lexer);
}

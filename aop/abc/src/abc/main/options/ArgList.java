/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.main.options;

import abc.main.*;
import java.util.*;

/** A list of command-line arguments.
 *
 *  @author Ondrej Lhotak
 */

public class ArgList extends LinkedList {
    public ArgList(String[] args) {
        for(int i = 0; i < args.length; i++) add(args[i]);
    }
    /** Return the current arg. */
    public String top() { return (String) getFirst(); }
    /** Return the argument of the current argument, or throw an exception
        * if there isn't one. */
    public String argTo() {
        String top = top();
        shift();
        if(isEmpty())
            throw new IllegalArgumentException("Missing argument to " + top);
        return top();
    }
    /** Move to the next argument. */
    public void shift() { removeFirst(); }
    /** Add arg to the front of the arg list. */
    public void push( String arg ) { addFirst(arg); }
}

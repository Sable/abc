/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

/** An advice specification states the kind of advice and the formals.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public abstract class AdviceSpec_c extends Node_c implements AdviceSpec
{
    protected List formals;
    protected TypeNode returnType;
    protected AdviceFormal returnVal;

    public AdviceSpec_c(Position pos, List formals, TypeNode returnType,
                        AdviceFormal returnVal)
    {
        super(pos);
        this.formals = formals;
                this.returnType = returnType;
                this.returnVal = returnVal;
    }

    public List formals() {
        return formals;
    }

    public TypeNode returnType() {
        return returnType;
    }
    
    public AdviceFormal returnVal() {
        return returnVal;
    }

    public void setReturnType(TypeNode rt) {
        returnType = rt;
    }

    public void setReturnVal(AdviceFormal rv) {
        returnVal = rv;
    }
}

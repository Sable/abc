/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

import abc.aspectj.types.AspectType;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;

/**
 * 
 * @author Oege de Moor
 *
 */
public abstract class PerClause_c extends Node_c implements PerClause,
							    MakesAspectMethods
{

    public PerClause_c(Position pos) {
        super(pos);
    }
    
	public int kind() {
		return AspectType.PER_NONE;
	}
	
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushFormals(new LinkedList());
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        visitor.popFormals();
        return this;
    }

}

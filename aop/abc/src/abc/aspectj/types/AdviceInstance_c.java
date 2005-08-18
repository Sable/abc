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

package abc.aspectj.types;

import java.util.List;
import java.util.Iterator;

import polyglot.util.Position;

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.ReferenceType;
import polyglot.types.Flags;


import polyglot.ext.jl.types.MethodInstance_c;

import abc.aspectj.ast.AdviceSpec;


/**
 * 
 * @author Oege de Moor
 *
 */
public class AdviceInstance_c extends MethodInstance_c
{
    protected String signature;
	
    /** Used for deserializing types. */
    protected AdviceInstance_c() { }

    public AdviceInstance_c(TypeSystem ts, Position pos,
                            ReferenceType container, Flags flags,
                            Type returnType, String name, List formalTypes,
                            List excTypes, String signature)
    {
 	    super(ts, pos, container, flags, returnType,
                name, formalTypes, excTypes);

 		this.signature = signature;
 	}	
 	
    public String toString()
    {
        String s = designator() + " " + flags.translate() +
                    signature();

        if (! excTypes.isEmpty()) {
            s += " throws ";

            for (Iterator i = excTypes.iterator(); i.hasNext(); ) {
                Type t = (Type) i.next();
                s += t.toString();

                if (i.hasNext())
                    s += ", ";
            }
        }

        return s;
    }
   
    public String signature() {
        return signature;
    }

    public String designator() {
        return "advice";
    }
}

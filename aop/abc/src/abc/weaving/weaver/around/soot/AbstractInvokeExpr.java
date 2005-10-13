/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Sascha Kuzins
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

package abc.weaving.weaver.around.soot;

import soot.*;
import soot.jimple.*;

import java.util.*;
import java.io.*;

abstract public class AbstractInvokeExpr implements InvokeExpr, ModifiableInvokeExpr
{
	public void addArguments(List args , List addedTypes) {
		for(int i = 0; i < args.size(); i++)
            this.argBoxes.add(Jimple.v().newImmediateBox((Value) args.get(i)));
		
		SootMethodRef oldRef=methodRef;
		List newTypes=new ArrayList(oldRef.parameterTypes()); // need to make copy
		newTypes.addAll(addedTypes);
		soot.SootMethodRef ref=Scene.v().makeMethodRef(
				oldRef.declaringClass(),
				oldRef.name(),
				newTypes,
				oldRef.returnType(),
				oldRef.isStatic()					
				);
		setMethodRef(ref);
	}
    protected SootMethodRef methodRef;
    protected List /*ValueBox*/ argBoxes;

	public void setMethodRef(SootMethodRef methodRef) {
		this.methodRef = methodRef;
	}
	
    public SootMethodRef getMethodRef()
    {
        return methodRef;
    }

    public SootMethod getMethod()
    {
        return methodRef.resolve();
    }

    public abstract Object clone();
    
    public Value getArg(int index)
    {
        return ((ValueBox)argBoxes.get(index)).getValue();
    }

    public List getArgs()
    {
        List l = new ArrayList(argBoxes.size());
        for (Iterator it=argBoxes.iterator(); it.hasNext();)
            l.add( ((ValueBox)it.next()).getValue());

        return l;
    }

    public int getArgCount()
    {
        return argBoxes.size();
    }

    
    public void setArg(int index, Value arg)
    {
        getArgBox(index).setValue(arg);
    }

    public ValueBox getArgBox(int index)
    {
        return (ValueBox)argBoxes.get(index);
    }

    public Type getType()
    {
        return methodRef.returnType();
    }
}

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

public class JInterfaceInvokeExpr extends abc.weaving.weaver.around.soot.AbstractInterfaceInvokeExpr
{
    public JInterfaceInvokeExpr(Value base, SootMethodRef methodRef, List args)
    {
        super(Jimple.v().newLocalBox(base), methodRef,
             new ArrayList(args.size()));

        for(int i = 0; i < args.size(); i++)
            this.argBoxes.add(Jimple.v().newImmediateBox((Value) args.get(i)));
    }

    public Object clone() 
    {
        List argList = new ArrayList(getArgCount());

        for(int i = 0; i < getArgCount(); i++) {
            argList.add(i, Jimple.cloneIfNecessary(getArg(i)));
        }
            
        return new  JInterfaceInvokeExpr(Jimple.cloneIfNecessary(getBase()), methodRef, argList);
    }

}

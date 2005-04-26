/*
 * Created on 25-Apr-2005
 */

/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package abc.weaving.weaver.around.soot;

import soot.*;
import soot.tagkit.*;
import soot.jimple.*;
import soot.baf.*;
import soot.util.*;
import java.util.*;
;


public abstract class AbstractStaticInvokeExpr extends abc.weaving.weaver.around.soot.AbstractInvokeExpr implements StaticInvokeExpr, ConvertToBaf
{
    AbstractStaticInvokeExpr(SootMethodRef methodRef, List args)
    {
        super.methodRef=methodRef;
        super.argBoxes=new ArrayList();

        for(int i = 0; i < args.size(); i++)
            this.argBoxes.add(Jimple.v().newImmediateBox((Value) args.get(i)));
    }

    public boolean equivTo(Object o)
    {
        if (o instanceof AbstractStaticInvokeExpr)
        {
            AbstractStaticInvokeExpr ie = (AbstractStaticInvokeExpr)o;
            if (!(getMethod().equals(ie.getMethod()) && 
                  argBoxes.size() == ie.argBoxes.size()))
                return false;
            for (int i = 0; i < argBoxes.size(); i++)
                if (!(getArg(i).equivTo(ie.getArg(i))))
                    return false;
            return true;
        }
        return false;
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    public int equivHashCode() 
    {
        return getMethod().equivHashCode();
    }

    public abstract Object clone();
    
    

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(Jimple.v().STATICINVOKE + " " + methodRef.getSignature() + "(");

        for(int i = 0; i < argBoxes.size(); i++)
        {
            if(i != 0)
                buffer.append(", ");

            buffer.append(getArg(i).toString());
        }

        buffer.append(")");

        return buffer.toString();
    }

    public void toString(UnitPrinter up)
    {
        up.literal(Jimple.v().STATICINVOKE);
        up.literal(" ");
        up.methodRef(methodRef);
        up.literal("(");

        for(int i = 0; i < argBoxes.size(); i++)
        {
            if(i != 0)
                up.literal(", ");

            getArgBox(i).toString(up);
        }

        up.literal(")");
    }

    public List getUseBoxes()
    {
        List list = new ArrayList();

        for(int i = 0; i < argBoxes.size(); i++)
        {
            list.addAll(getArg(i).getUseBoxes());
            list.add(getArgBox(i));
        }

        return list;
    }

    public void apply(Switch sw)
    {
        ((ExprSwitch) sw).caseStaticInvokeExpr(this);
    }

    public void convertToBaf(JimpleToBafContext context, List out)
    {
       for(int i = 0; i < argBoxes.size(); i++)
        {
            ((ConvertToBaf)(getArg(i))).convertToBaf(context, out);
        }
       
       Unit u;
       out.add(u = Baf.v().newStaticInvokeInst(methodRef));

       Unit currentUnit = context.getCurrentUnit();

	Iterator it = currentUnit.getTags().iterator();	
	while(it.hasNext()) {
	    u.addTag((Tag) it.next());
	}
    }
}

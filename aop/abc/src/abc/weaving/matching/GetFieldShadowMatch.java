/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.weaver.*;
import polyglot.util.InternalCompilerError;

/** A get field join point shadow.
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @author Oege de Moor
 */
public class GetFieldShadowMatch extends StmtShadowMatch {
    
    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new GetFieldShadowMatch(cim.target(), cim.map(stmt), fieldref);
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }
    private SootFieldRef fieldref;
    
    private GetFieldShadowMatch(SootMethod container,Stmt stmt,SootFieldRef fieldref) {
	super(container,stmt);
	if(abc.main.Debug.v().java13) fieldref=fieldref.resolve().makeRef();
	this.fieldref=fieldref;
    }

    public SootFieldRef getFieldRef() {
	return fieldref;
    }

    public static GetFieldShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("GetField");

	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	if (!(stmt instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) stmt;
	Value rhs = as.getRightOp();
       	if(rhs instanceof FieldRef) {
	    FieldRef fr = (FieldRef) rhs;
		if (MethodCategory.weaveSetGet(fr.getFieldRef()))
		    return new GetFieldShadowMatch(pos.getContainer(),stmt,fr.getFieldRef());
	    else
	    	return null;
	} else if(rhs instanceof InvokeExpr) {
		InvokeExpr ie = (InvokeExpr) rhs;
		SootMethodRef smr = ie.getMethodRef();
	    if(MethodCategory.getCategory(smr)
	       ==MethodCategory.ACCESSOR_GET) {
		return new GetFieldShadowMatch
		    (pos.getContainer(),stmt,MethodCategory.getFieldRef(smr));
	    }
	    else return null;
	} else {
	    return null;
	}
    }

    
    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ("field-get",
             "org.aspectj.lang.reflect.FieldSignature",
             "makeFieldSig",
	     AbcSJPInfo.makeFieldSigData(fieldref),stmt);
    }




    public ContextValue getTargetContextValue() {
		if (stmt instanceof AssignStmt) {
			// System.out.println(stmt);
			AssignStmt a = (AssignStmt) stmt;
			Value rhs = a.getRightOp();
			if (rhs instanceof FieldRef) {
				FieldRef fr=(FieldRef) rhs;
				if(!(fr instanceof InstanceFieldRef)) return null;
				InstanceFieldRef ifr=(InstanceFieldRef) fr;
				return new JimpleValue((Immediate)ifr.getBase());
			} else if (rhs instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr vie = (InstanceInvokeExpr) rhs;
				if (MethodCategory.getCategory(vie.getMethodRef()) 
				    == MethodCategory.ACCESSOR_GET)
					return new JimpleValue((Immediate)vie.getBase());
			} 
		} else if (stmt instanceof InvokeStmt) {
			InvokeExpr ie = ((InvokeStmt)stmt).getInvokeExpr();
			if (ie instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
				if (MethodCategory.getCategory(vie.getMethodRef()) 
				    == MethodCategory.ACCESSOR_GET)
					return new JimpleValue((Immediate)vie.getBase());
			}
		}
		return null;
    }

    public ContextValue getReturningContextValue() {
	return new JimpleValue((Immediate) ((AssignStmt) stmt).getLeftOp());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	return new ArrayList(0);
    }

    public String joinpointName() {
	return "field get";
    }


}

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

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.Chain;

import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.weaver.*;

/** The results of matching at a field set shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @author Oege de Moor
 */
public class SetFieldShadowMatch extends StmtShadowMatch {
    
    private SootFieldRef fieldref;

    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new SetFieldShadowMatch(cim.target(), cim.map(stmt), fieldref);
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }

    private SetFieldShadowMatch(SootMethod container,Stmt stmt,SootFieldRef fieldref) {
	super(container,stmt);
	if(abc.main.Debug.v().java13) fieldref=fieldref.resolve().makeRef();
	this.fieldref=fieldref;
    }

    public SootFieldRef getFieldRef() {
	return fieldref;
    }

    public static SetFieldShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("SetField");

	Stmt stmt=((StmtMethodPosition) pos).getStmt();
	
	SootFieldRef sfr = null;
	if(stmt instanceof AssignStmt) {
		AssignStmt as = (AssignStmt) stmt;
		Value lhs = as.getLeftOp();
		if (lhs instanceof FieldRef) {
			FieldRef fr = (FieldRef) lhs;
			sfr = fr.getFieldRef();
			if (!MethodCategory.weaveSetGet(sfr))
				return null;
			makeLocalForRHS(((StmtMethodPosition) pos).getContainer(), as);
		} else return null;
	} else if (stmt instanceof InvokeStmt) {
		InvokeStmt is = (InvokeStmt) stmt;
		// System.out.println("stmt="+stmt);
		InvokeExpr ie = is.getInvokeExpr();
		SootMethodRef smr = ie.getMethodRef();
		if(MethodCategory.getCategory(smr)
				   ==MethodCategory.ACCESSOR_SET) {
					sfr = MethodCategory.getFieldRef(smr);
					// FIXME: make local for argument?
		}
		else return null;
	} else return null;

        // If a final static field is being initialised with a compile-time
        // constant, no join point exists at sets to it. In practice it
        // is unlikely, but not illegal, that there will be any sets -
        // the only place there could is the static initialiser, and most
        // if not all compilers including abc omit it.

        // Unfortunately there doesn't seem to be any better way to do this,
        // despite the tags all being subclasses of ConstantValueTag
        SootField resolved=sfr.resolve();
        if(resolved.hasTag("IntegerConstantValueTag") ||
           resolved.hasTag("LongConstantValueTag") ||
           resolved.hasTag("FloatConstantValueTag") ||
           resolved.hasTag("DoubleConstantValueTag") ||
           resolved.hasTag("StringConstantValueTag")) 
            return null;

        return new SetFieldShadowMatch(pos.getContainer(),stmt,sfr);
    }
    /**
     * Ensures that the rhs of the set is a local.
     * Needed for around().
     * @param method
     * @param stmt
     */
    private static void makeLocalForRHS(SootMethod method, AssignStmt stmt) {
		Value val=stmt.getRightOp();
    	if (!(val instanceof Local)) {
			Body body=method.getActiveBody();
			Chain statements=body.getUnits().getNonPatchingChain();
			LocalGeneratorEx lg=new LocalGeneratorEx(body);
			
			Local l=lg.generateLocal(stmt.getLeftOp().getType(),
								"setRHSLocal");
			AssignStmt as=Jimple.v().newAssignStmt(l,val);
			statements.insertBefore(as, stmt);
			stmt.setRightOp(l);
			stmt.redirectJumpsToThisTo(as);
    	}
    }
    

    
    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ("field-set",
             "org.aspectj.lang.reflect.FieldSignature",
             "makeFieldSig",
	     AbcSJPInfo.makeFieldSigData(fieldref),stmt);
    }
       
	public ContextValue getTargetContextValue() {
		// System.out.println(stmt);
		if (stmt instanceof AssignStmt) {
			// System.out.println(stmt);
			AssignStmt a = (AssignStmt) stmt;
			Value lhs = a.getLeftOp();
			if (lhs instanceof FieldRef) {
				FieldRef fr=(FieldRef) lhs;
				if(!(fr instanceof InstanceFieldRef)) return null;
				InstanceFieldRef ifr=(InstanceFieldRef) fr;
				return new JimpleValue((Immediate)ifr.getBase());
			}
			Value rhs = a.getRightOp();
			if (rhs instanceof InvokeExpr) {
			InstanceInvokeExpr vie = (InstanceInvokeExpr) rhs;
			if (MethodCategory.getCategory(vie.getMethodRef()) 
			    == MethodCategory.ACCESSOR_SET)
				return new JimpleValue((Immediate) vie.getBase());
		}
		} else if (stmt instanceof InvokeStmt) {
			InvokeExpr ie = ((InvokeStmt)stmt).getInvokeExpr();
			if (ie instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
				if (MethodCategory.getCategory(vie.getMethodRef()) 
				    == MethodCategory.ACCESSOR_SET)
					return new JimpleValue((Immediate) vie.getBase());
			}

		}
		return null;
	}

    public List/*<ContextValue>*/ getArgsContextValues() {
	ArrayList ret=new ArrayList(1);
	if (stmt instanceof AssignStmt) {
		AssignStmt a = (AssignStmt) stmt;
		if (a.getLeftOp() instanceof FieldRef)
			ret.add(new JimpleValue((Immediate) ((AssignStmt) stmt).getRightOp()));
		Value rhs = a.getRightOp();
		if (rhs instanceof InvokeExpr) {
			InvokeExpr vie = (InvokeExpr) rhs;
			if (MethodCategory.getCategory(vie.getMethodRef()) 
			    == MethodCategory.ACCESSOR_SET)
				ret.add(new JimpleValue((Immediate) vie.getArg(0)));
		}
	} else if (stmt instanceof InvokeStmt) {
		InvokeExpr ie = ((InvokeStmt)stmt).getInvokeExpr();
		ret.add(new JimpleValue((Immediate)ie.getArg(0)));
	} else throw new InternalCompilerError("stmt neither an assignment nor an invoke");
	return ret;
    }

    public String joinpointName() {
	return "field set";
    }



}

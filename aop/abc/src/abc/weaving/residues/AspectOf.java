/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.residues;

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.main.Debug;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Singleton;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.AdviceWeavingContext;
import abc.weaving.weaver.ConstructorInliningMap;
import abc.weaving.weaver.WeavingContext;

/** A residue that puts the relevant aspect instance into a 
 *  local variable in the weaving context
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @author Sascha Kuzins
 */ 

public class AspectOf extends Residue {

    private SootClass aspct;
    public Residue optimize() { return this; }
    public Residue inline(ConstructorInliningMap cim) {
        if(pervalue == null) return new AspectOf(aspct, null);
        return new AspectOf(aspct, pervalue.inline(cim));
    }

    // (null to indicate singleton aspect; i.e. no params to aspectOf)
    // This is not true: percflow aspects don't have a pervalue either. (Sascha)
    private ContextValue pervalue;

    public AspectOf(SootClass aspct,ContextValue pervalue) {
	this.aspct=aspct;
	this.pervalue=pervalue;
    }

    private boolean isSingletonAspect() {
    	Aspect a=abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAspectFromSootClass(aspct);
    	return a.getPer() instanceof Singleton;
    }
    
    public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen,
			Chain units, Stmt begin, Stmt fail, boolean sense, WeavingContext wc) {

		// We don't expect the frontend/matcher to produce a residue that does
		// this.
		// There's no reason we couldn't just do the standard "automatic fail"
		// thing
		// if there was ever a need, though.
		if (!sense)
			throw new InternalCompilerError(
					"aspectOf residue should never be used negated");

		List paramTypes;
		List params;
		Local aspectref;
		Stmt aspectOfInsertionPoint;
		Stmt lastStmt=null;
		if (isSingletonAspect() && !Debug.v().disableAspectOfOpt) {
//			 For singleton aspects, create one shared local,
			// initialize it to null at the beginning of the method
			// and perform the aspectOf call lazily.
			// The nullcheck-eliminator will reduce the number of checks later
			// on.
					
			params = new ArrayList();
			paramTypes = new ArrayList();

			//aspectref = localgen.generateLocal(aspct.getType(),"theAspect");
			String mangledLocalName = "theAspect$" + aspct.getType().toString();
			aspectref = localgen.getLocalByName(mangledLocalName);
			if (aspectref == null) {
				aspectref = localgen.generateLocalWithExactName(
						aspct.getType(), mangledLocalName);
				AssignStmt init = Jimple.v().newAssignStmt(aspectref,
						NullConstant.v());
				Stmt s = Restructure.findFirstRealStmtOrNull(method, units);
				if (s == null)
					units.addFirst(init);
				else
					units.insertBefore(init, s);
			}
			NopStmt skip = Jimple.v().newNopStmt();
			units.insertAfter(skip, begin);
			IfStmt check = Jimple.v().newIfStmt(
					Jimple.v().newNeExpr(aspectref, NullConstant.v()), skip);
			units.insertAfter(check, begin);
			aspectOfInsertionPoint=check; 
			lastStmt=skip;
		} else if (pervalue == null) {
			params=new ArrayList(); paramTypes=new ArrayList();
			aspectref = localgen.generateLocal(aspct.getType(), "theAspect");
			aspectOfInsertionPoint=begin;
		} else {
			params = new ArrayList(1);
			paramTypes = new ArrayList(1);
			paramTypes
					.add(Scene.v().getSootClass("java.lang.Object").getType());
			params.add(pervalue.getSootValue());

			aspectref = localgen.generateLocal(aspct.getType(), "theAspect");
			aspectOfInsertionPoint=begin;
		}

		AssignStmt stmtAspectOf = Jimple.v().newAssignStmt(
				aspectref,
				Jimple.v().newStaticInvokeExpr(
						Scene.v().makeMethodRef(aspct, "aspectOf", paramTypes,
								aspct.getType(), true), params));
		//if(wc.kindTag == null) {
        wc.setKindTag(InstructionKindTag.ADVICE_ARG_SETUP);
        //}
        Tagger.tagStmt(stmtAspectOf, wc);
		if (lastStmt==null)
			lastStmt=stmtAspectOf;

		units.insertAfter(stmtAspectOf, aspectOfInsertionPoint);

		((AdviceWeavingContext) wc).aspectinstance = aspectref;

		return lastStmt;
	}

    public String toString() {
	return "aspectof("+aspct+","+pervalue+")";
    }

}

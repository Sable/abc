/* abc - The AspectBench Compiler
 * Copyright (C) 2004 pavel
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

/*
 * Created on Aug 2, 2004
 */
package abc.soot.util;

import java.util.ArrayList;

import polyglot.util.UniqueID;
import soot.Body;
import soot.Local;
import soot.MethodSource;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SootFieldRef;
import soot.Type;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.MethodSig;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.util.Chain;

/** Creates any accessor methods to access a qualified special (<code>super</code> or <code>this</code>) 
 * from a class. Some methods may/will be added to textually enclosing classes.
 * 
 * @author pavel
 */
public class QualSpecialAccessorMethodSource implements MethodSource {
    boolean qualThisNotSuper;
    MethodSig method;
    soot.SootClass target;
    soot.SootClass qualifier;
    
    public QualSpecialAccessorMethodSource(MethodSig method, soot.SootClass target, 
                soot.SootClass qualifier, boolean qualThisNotSuper) {
        this.method = method;
        this.target = target;
        this.qualifier = qualifier;
        this.qualThisNotSuper = qualThisNotSuper;
    }
    
    public Body getBody(SootMethod sootMethod, String phaseName) {
        soot.Body body = Jimple.v().newBody(sootMethod);
        LocalGenerator lg = new LocalGenerator(body);
        ThisRef tr = Jimple.v().newThisRef(target.getType());
        soot.Local thisloc = lg.generateLocal(target.getType());
        IdentityStmt ids = Jimple.v().newIdentityStmt(thisloc, tr);
        body.getUnits().add(ids);
        soot.Local v = getThis(body, qualifier.getType());
        ReturnStmt ret = Jimple.v().newReturnStmt(v); 
        body.getUnits().add(ret);
        return body;
    }

    /** return a local that contains "qualifier.this", adding the relevant statements to the body. 
     *  This code is adapted from JavaToJimple.JimpleBodyBuilder.
     */ 
    private Local getThis(Body b, Type sootType){
        // if we need the current 'this', just return it
        if(b.getThisLocal().getType().equals(sootType))
            return b.getThisLocal();
        //otherwise get this$0 from one level up
        soot.SootClass classToInvoke = ((soot.RefType)b.getThisLocal().getType()).getSootClass();
        soot.SootFieldRef outerThisField = soot.Scene.v().makeFieldRef(classToInvoke, "this$0", classToInvoke.getOuterClass().getType(),false); 
        Local t1 = Jimple.v().newLocal(UniqueID.newID("this$0$loc"), outerThisField.type());
        b.getLocals().add(t1);
        
        FieldRef fieldRef = Jimple.v().newInstanceFieldRef(b.getThisLocal(), outerThisField);
        AssignStmt fieldAssignStmt = Jimple.v().newAssignStmt(t1, fieldRef);
        b.getUnits().add(fieldAssignStmt);
        
        // make new access methods for "this" in textually enclosing classes
        soot.Local t2 = t1;
        while(!t2.getType().equals(sootType)) {
            classToInvoke = ((soot.RefType)t2.getType()).getSootClass();
            // create an accessor method for the private this$0 field in that class
            SootMethodRef methToInvoke = makeOuterThisAccessMethod(classToInvoke);
            // invoke it
            Local t3 = Jimple.v().newLocal(UniqueID.newID("invoke$loc"), methToInvoke.returnType());
            b.getLocals().add(t3);
            InvokeExpr ie = Jimple.v().newVirtualInvokeExpr(t2, methToInvoke);
            AssignStmt rStmt = Jimple.v().newAssignStmt(t3, ie);
            b.getUnits().add(rStmt);
            // next iteration
            t2 = t3;
        }
        return t2;
    }
    
	private soot.SootMethodRef makeOuterThisAccessMethod(soot.SootClass classToInvoke){
		// create the method
		String name = UniqueID.newID("access$this$0$");
		ArrayList paramTypes = new ArrayList();
        soot.SootFieldRef sf = soot.Scene.v().makeFieldRef(classToInvoke, "this$0", classToInvoke.getOuterClass().getType(),false); 
		SootMethod meth = new SootMethod(name, paramTypes, sf.type(), soot.Modifier.PUBLIC);
		//	add to target class
		classToInvoke.addMethod(meth);
		// now fill in the body
		Body b = Jimple.v().newBody(meth); meth.setActiveBody(b);
		Chain ss = b.getUnits(); Chain ls = b.getLocals();
		// generate local for "this"
		ThisRef thiz = Jimple.v().newThisRef(classToInvoke.getType());
		Local thizloc = Jimple.v().newLocal("this$loc",classToInvoke.getType()); ls.add(thizloc);
		IdentityStmt ids = Jimple.v().newIdentityStmt(thizloc,thiz); ss.add(ids);
		// assign res = this.this$0
		SootFieldRef thisField = soot.Scene.v().makeFieldRef(classToInvoke, "this$0", classToInvoke.getType(),false);
		FieldRef fr = Jimple.v().newInstanceFieldRef(thizloc, thisField); 
		Local res = Jimple.v().newLocal("result",sf.type()); ls.add(res);
		AssignStmt astmt = Jimple.v().newAssignStmt(res,fr); ss.add(astmt);
		// return res
		ReturnStmt ret = Jimple.v().newReturnStmt(res); ss.add(ret);	
		
		return meth.makeRef();
	}

}

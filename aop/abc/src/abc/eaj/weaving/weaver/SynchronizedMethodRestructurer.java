/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.eaj.weaving.weaver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.ClassConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import abc.main.Debug;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * This is necessary for lock/unlock pointcuts.
 * For those pointcuts, synchronized methods have to be restructured the following way.
 * 1.) Remove the <i>syncrhonized</i> modifier.
 * 2.) Wrap the entire body into something equivalent to a synchronized block.
 * 
 * The reason is that otherwise for synchronized methods you get the following semantics:
 * <br>
 * before(): execution(synchronized * *(..)) {..}<br>
 *   advice executes <i>after</i> the lock has been taken<br>
 * after(): execution(synchronized * *(..)) {..}<br>
 *   advice executes before the lock is released<br>
 * Cannot advise (apart from with 'call') before the lock has been taken or
 * after it has been released!
 * 
 * @author Eric Bodden
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=123759
 */
public class SynchronizedMethodRestructurer {

	/** 
	 * Transforms all concrete methods in all weavable classes for which the {@link MethodCategory}
	 * says that it can be woven inside.
	 */
	public void apply() {
		SyncWarningWeaver.reset();
		GlobalAspectInfo gai = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
		for (Iterator weavableIter = gai.getWeavableClasses().iterator(); weavableIter.hasNext();) {
			AbcClass abcClass = (AbcClass) weavableIter.next();
			SootClass sc = abcClass.getSootClass();
			
			for (Iterator methodIter = sc.methodIterator(); methodIter.hasNext();) {
				SootMethod method = (SootMethod) methodIter.next();
				if(MethodCategory.weaveInside(method) && method.isConcrete()) {
					transform(method.getActiveBody());
				}
			}
		}
		
		Debug.v().dontCheckExceptions = true;
	}

	/**
	 * Transforms b.
	 * @param b some Jimple body
	 */
	protected void transform(Body b) {
		
		SootMethod method = b.getMethod();
		if(!Modifier.isSynchronized(method.getModifiers()))
			return;
				
		//remove "synchronized modifier from method declaration"
		method.setModifiers((method.getModifiers() & ~Modifier.SYNCHRONIZED));
			
		Local thisLocal = null;
		if(!method.isStatic()) {
			thisLocal = moveIdentityStatementForward(b);
		}

		PatchingChain units = b.getUnits();
		LocalGenerator localGenerator = new LocalGenerator(b);

		//enter entermonitor(this), resp. entermonitor(<CurrType>.class) to beginning
		Local syncLocal;
		//in static methods, we synchronize on the class, else on "this"
		if(method.isStatic()) {
			RefType classType = Scene.v().getSootClass("java.lang.Class").getType();
			syncLocal = localGenerator.generateLocal(classType);
		} else {
			syncLocal = thisLocal;
		}
		//find first statement after all identity statements
		Stmt firstRealStmt = Restructure.findFirstRealStmt(method, units);
		//add "entermonitor(thisLocal)"
		EnterMonitorStmt monitorEnterStmt = Jimple.v().newEnterMonitorStmt(syncLocal);
		units.insertBefore(monitorEnterStmt,firstRealStmt);
		//add nop
		NopStmt beginRealBody = Jimple.v().newNopStmt();
		units.insertAfter(beginRealBody,monitorEnterStmt);
		//in static methods, we synchronize on the class, else on "this"
		if(method.isStatic()) {
			ClassConstant classConstant = ClassConstant.v(method.getDeclaringClass().getName());
			units.addFirst(Jimple.v().newAssignStmt(syncLocal, classConstant)); //add "thisLocal = @this" before
		} 
		
		//make sure we only have a single return statement
		Stmt nopBeforeReturn = Restructure.restructureReturn(method);
		Stmt retStmt = (Stmt) units.getSuccOf(nopBeforeReturn);
		
		//Throwable caughtEx;
		SootClass throwable = Scene.v().getSootClass("java.lang.Throwable");
		RefType excType = throwable.getType();
		Local excLocal = localGenerator.generateLocal(excType);
		
		//rethrow exception right before return:
		//caughtEx = @caughtexception
		//monitorexit(thisLocal)
		//throw caughtEx
		List newStatements = new ArrayList();
		IdentityStmt exceptionIdStmt = Jimple.v().newIdentityStmt(excLocal, Jimple.v().newCaughtExceptionRef());
		newStatements.add(exceptionIdStmt);
		newStatements.add(Jimple.v().newExitMonitorStmt(syncLocal));
		newStatements.add(Jimple.v().newThrowStmt(excLocal));
		units.insertAfter(newStatements, nopBeforeReturn);
		
		//add the following for the "normal exit"
		//monitorexit(thisLocal)
		//goto retStmt
		newStatements.clear();
		ExitMonitorStmt exitMon = Jimple.v().newExitMonitorStmt(syncLocal);
		newStatements.add(exitMon);
		newStatements.add(Jimple.v().newGotoStmt(retStmt));
		units.insertAfter(newStatements, nopBeforeReturn);

		//insert trap: from right after the entermonitor to the first exitmonitor, jumping to the assignment of the exception
		b.getTraps().add(Jimple.v().newTrap(throwable, beginRealBody, exitMon, exceptionIdStmt));

		if(Debug.v().doValidate) {
			b.validate();
		}
		
		//register method as rewritten to generate warning
		SyncWarningWeaver.registerConvertedMethod(method);
	}

	/**
	 * If an identity statement assigning <i>this</i> exists in b, this statement is moved to be the very first statement.
	 * If not, such a statement is created.
	 * If multiple such statements exist, only the first one is moved. 
	 * @param b any Jimple body
	 * @return the {@link Local} this identity statement assigns to 
	 */
	private Local moveIdentityStatementForward(Body b) {
		IdentityStmt firstIdentityStmt = null;
		for (Iterator unitIter = b.getUnits().iterator(); unitIter.hasNext();) {
			Stmt stmt = (Stmt) unitIter.next();
			if(stmt instanceof IdentityStmt) {
				IdentityStmt identityStmt = (IdentityStmt) stmt;
				if(identityStmt.getRightOp() instanceof ThisRef) {
					firstIdentityStmt = identityStmt;					
					break;
				}
			}
		}
		if(firstIdentityStmt!=null) {
			b.getUnits().remove(firstIdentityStmt);
		} else {
			RefType thisType = b.getMethod().getDeclaringClass().getType();
			Local thisLocal = new LocalGenerator(b).generateLocal(thisType);
			ThisRef newThisRef = Jimple.v().newThisRef(thisType);
			firstIdentityStmt = Jimple.v().newIdentityStmt(thisLocal, newThisRef);
		}
		
		b.getUnits().addFirst(firstIdentityStmt);
		
		return (Local) firstIdentityStmt.getLeftOp();
	}
}

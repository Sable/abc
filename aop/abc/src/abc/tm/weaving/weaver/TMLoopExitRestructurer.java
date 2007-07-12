/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
 
 package abc.tm.weaving.weaver;

import java.util.Collection;
import java.util.Iterator;

import soot.Body;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.GotoStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import abc.main.Debug;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * For statements that are targets of loop exists, inserts nop-statements.
 * This is in order to allow shadows to be rerouted to those positions.
 * @author Eric Bodden
 */
public class TMLoopExitRestructurer {

	/** 
	 * Transforms all concrete methods in all weavable classes for which the {@link MethodCategory}
	 * says that it can be woven inside.
	 */
	public static void apply() {
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
	}

	/**
	 * We do the following transformation. We define the target of a loop exit as a statement
	 * that is reached from a loop but is not part of the loop. This can be either (a) a fall-through
	 * of a statement <code>if(x) goto loop-header</code> or the target of a branch statement.
	 * 
	 * First, for each target or a loop exit, add a nop before the target (not rerouting jumps).
	 * Then we add a goto to the original target. Finally, the loop exit itself, if it is a branch statement,
	 * is rerouted to the nop that was inserted. 
	 * 
	 * For instance, if we have two loop exists <code>if(x) goto labeli;</code> and <code>if(y) goto labeli;</code>,
	 * we would change the code to the following:
	 * 
	 * <code>
	 * if(x) goto label1;
	 * ...
	 * if(y) goto labeli;
	 * ...
	 * label1:
	 *   nop;
	 *   goto label3;
	 * label2:
	 *   nop;
	 *   goto labeli;
	 * </code>
	 * 
	 */
	protected static void transform(Body b) {
		
	    PatchingChain<Unit> units = b.getUnits();
	    
	    //for all loops in the body
	    LoopNestTree loopNestTree = new LoopNestTree(b);
	    for (Loop loop : loopNestTree) {
	        	        
	        Collection<Stmt> loopExits = loop.getLoopExits();
	        //for all loop exists
	        //(loop exists can either be a goto statement or a fall-through through an if-statement)
	        for (Stmt exit : loopExits) {
	            Collection<Stmt> targetsOfLoopExit = loop.targetsOfLoopExit(exit);
	            //for each successor statement of the loop exit
	            for (Stmt target : targetsOfLoopExit) {
	                //insert a nop
	                NopStmt targetNop = Jimple.v().newNopStmt();
	                units.insertBeforeNoRedirect(targetNop, target);
	                //and insert a goto to the target;
	                //this *might* just jump to the next statement but not necessarily, for instance not if multiple loop
	                //exits jump to the same target
                    GotoStmt gotoTarget = Jimple.v().newGotoStmt(target);
	                units.insertBeforeNoRedirect(gotoTarget, target);
	                //patch the exit (and the exit only!) to jump to the nop instead 
	                if(exit.branches()) {
	                    for (UnitBox box : exit.getUnitBoxes()) {
                            Unit jumpTarget = box.getUnit();
                            if(target==jumpTarget) {
                                box.setUnit(targetNop);
                            }
                        }
	                }
                }
            }
        }
	    
        if(Debug.v().doValidate) {
            b.validate();
        }        
	}

}

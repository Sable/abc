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
import java.util.List;

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
	                if(units.getSuccOf(exit).equals(target)) {
	                    //we have an exit of the form "if(p) goto header", i.e. we exit the loop by falling through;
	                    //just add a nop in this case - that's all
	                    units.insertAfter(Jimple.v().newNopStmt(),exit);
	                } else {
	                    /* we exit by "if(p) goto <target>" (where <target> is outside the loop)
	                     * convert this block...
	                     * 
	                     *   if(p) goto <target>
	                     *   <fallThrough>
	                     * 
	                     * ... to ...
	                     * 
	                     *   if(p) goto label1
	                     *   goto label2
	                     * label1:
	                     *   nop
	                     *   goto <target>
	                     * label2:
	                     *   <fallThrough>
	                     */
	                    Unit fallThrough = units.getSuccOf(exit);
	                    GotoStmt gotoFallThrough = Jimple.v().newGotoStmt(fallThrough);
                        units.insertAfter(gotoFallThrough, exit);
                        NopStmt nop = Jimple.v().newNopStmt();
                        units.insertAfter(nop, gotoFallThrough);
                        List<UnitBox> unitBoxes = exit.getUnitBoxes();
                        assert unitBoxes.size()==1;
                        assert unitBoxes.get(0)==target;
                        unitBoxes.get(0).setUnit(nop);
                        Unit gotoTarget = Jimple.v().newGotoStmt(target); 
                        units.insertAfter(gotoTarget, nop);
	                }
                }
            }
        }
	    
        if(Debug.v().doValidate) {
            b.validate();
        }        
	}

}

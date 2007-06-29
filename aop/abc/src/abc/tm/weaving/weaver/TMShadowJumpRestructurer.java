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

import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * Inserts Nop-statements at all targets of jumps in all weavable methods.
 * This is in order to allow shadows to be rerouted to those positions.
 * @author Eric Bodden
 */
public class TMShadowJumpRestructurer {

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
        for (Iterator<Unit> iter = b.getUnits().snapshotIterator(); iter.hasNext();) {
            Unit unit = iter.next();
            
            boolean isBranchTarget = false;
            //if the unit is referred to (presumably in a goto statement or if statement)
            List<UnitBox> boxesPointingToThis = unit.getBoxesPointingToThis();
            for (UnitBox box : boxesPointingToThis) {
                if(box.isBranchTarget()) {
                    isBranchTarget = true;
                    break;
                }
            }
            
            //also need to take into accoun cases where there is a predecessor unit and
            //this unit may be the end of a loop but may fall through
            //(this can only be the case if it's an if-statement)
            if(!isBranchTarget) {
                if(units.getFirst()!=unit && (units.getPredOf(unit) instanceof IfStmt)) {
                    isBranchTarget = true;
                }
            }
            
            if(isBranchTarget) {
                
                //insert a new nop statement before jump the target;
                //this automatically reroutes all jumps going to the unit to the nop instead 
                NopStmt targetNop = Jimple.v().newNopStmt();
                units.insertBefore(targetNop, unit);

            }
        }
        
        assert onlyJumpsToNops(units); 
	}

    protected static boolean onlyJumpsToNops(PatchingChain<Unit> units) {
        for (Unit unit : units) {
            List<UnitBox> unitBoxes = unit.getUnitBoxes();
            for (UnitBox unitBox : unitBoxes) {
                if(unitBox.isBranchTarget()) {
                    if(!(unitBox.getUnit() instanceof NopStmt)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}

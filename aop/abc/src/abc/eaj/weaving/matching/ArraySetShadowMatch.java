/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Pavel Avgustinov
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

package abc.eaj.weaving.matching;

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Immediate;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.matching.MethodPosition;
import abc.weaving.matching.SJPInfo;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.ShadowType;
import abc.weaving.matching.StmtMethodPosition;
import abc.weaving.matching.StmtShadowMatch;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.weaver.ConstructorInliningMap;

/**
 * @author Pavel Avgustinov
 */

public class ArraySetShadowMatch extends StmtShadowMatch {
    public ArraySetShadowMatch(SootMethod container, Stmt stmt) {
        super(container, stmt);
    }

    public static ShadowType shadowType()
    {
        return new ShadowType() {
            public ShadowMatch matchesAt(MethodPosition pos) {
                return ArraySetShadowMatch.matchesAt(pos);
            }
        };
    }

    public static ArraySetShadowMatch matchesAt(MethodPosition pos) {
        if (!(pos instanceof StmtMethodPosition)) return null;
        if (abc.main.Debug.v().traceMatcher) System.err.println("ArraySet");

        // In Jimple: * Assignment to an array occurs from a local
        //			  * The arrayref is the only thing on the lhs.

        Stmt stmt = ((StmtMethodPosition) pos).getStmt();

        if (!(stmt instanceof AssignStmt)) return null;
        AssignStmt assign=(AssignStmt)stmt;
        Value lhs = assign.getLeftOp();

        if(!(lhs instanceof ArrayRef)) return null;
        ArrayRef ref=(ArrayRef)lhs;

        Value index=ref.getIndex();
        // make sure the index is a local.
        // restructure if necessary.
        if (!(index instanceof Local)) {
            Body body=pos.getContainer().getActiveBody();
            Chain statements=body.getUnits().getNonPatchingChain();
            LocalGeneratorEx lg=new LocalGeneratorEx(body);
            Local l=lg.generateLocal(index.getType());
            AssignStmt as=Jimple.v().newAssignStmt(l, index);
            statements.insertBefore(as,stmt);
            stmt.redirectJumpsToThisTo(as);
            ref.setIndex(l);
        }

        return new ArraySetShadowMatch(pos.getContainer(), stmt);
    }
    
    // Set the RHS of the assignment as the joinpoint return value (the new value). This is always a local.
    public ContextValue getReturningContextValue() {
    	return new JimpleValue((Immediate) ((AssignStmt)stmt).getRightOp());
    }
    
    // set the index of the array access as the joinpoint first argument
    public List getArgsContextValues() {
    	ArrayList ret = new ArrayList(1);
    	
    	ArrayRef ref = (ArrayRef) ((AssignStmt)stmt).getLeftOp();
    	ret.add(new JimpleValue((Immediate)ref.getIndex()));
    	
    	return ret;
    }

    // set the array itself as the target
	public ContextValue getTargetContextValue() {
		ArrayRef ref = (ArrayRef) ((AssignStmt) stmt).getLeftOp();
		return new JimpleValue((Immediate)ref.getBase());
	}

	public ShadowMatch inline(ConstructorInliningMap cim) {
		ShadowMatch ret = cim.map(this);
		if(ret != null) return ret;
		if(cim.inlinee() != container) throw new InternalCompilerError("inlinee " + cim.inlinee() + 
				"doesn't match container " + container);
		ret = new ArraySetShadowMatch(cim.target(), cim.map(stmt));
		cim.add(this, ret);
		return ret;
	}

	public String joinpointName() {
		return "arrayset";
	}

	protected SJPInfo makeSJPInfo() {
		return abc.main.Main.v().getAbcExtension().createSJPInfo("arrayset",
				"org.aspectbench.eaj.lang.reflect.ArraySetSignature", "makeArraySetSig", 
				ExtendedSJPInfo.makeArraySetSigData(container), stmt);
	}

}

/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
import soot.Immediate;
import soot.SootMethod;
import soot.Value;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.Stmt;
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
 * Shadow match which matches entermonitor bytecodes. Exposes the value that is synchronized on.
 * @author Eric Bodden
 */
public class LockShadowMatch extends StmtShadowMatch {

    private LockShadowMatch(SootMethod container, Stmt stmt)
    {
        super(container, stmt);
    }

    public static ShadowType shadowType()
    {
        return new ShadowType() {
            public ShadowMatch matchesAt(MethodPosition pos) {
                return LockShadowMatch.matchesAt(pos);
            }
        };
    }

    public static LockShadowMatch matchesAt(MethodPosition pos)
    {
        if (!(pos instanceof StmtMethodPosition)) return null;
        if (abc.main.Debug.v().traceMatcher) System.err.println("Lock");

        Stmt stmt = ((StmtMethodPosition) pos).getStmt();

        if (!(stmt instanceof EnterMonitorStmt)) return null;
        Value rhs = ((EnterMonitorStmt) stmt).getOp();
        
        if(!(rhs instanceof Immediate)) {
        	throw new IllegalStateException("Expected value of type Immediate at rhs of entermonitor statement.");
        }        
        return new LockShadowMatch(pos.getContainer(), stmt);
    }

    public ContextValue getTargetContextValue()
    {
        return null;
    }
    
    public List getArgsContextValues() {
    	ArrayList ret = new ArrayList(1);    	
    	EnterMonitorStmt enterMonitor = (EnterMonitorStmt) stmt;
    	ret.add(new JimpleValue((Immediate)enterMonitor.getOp()));    	
    	return ret;
    }

    public SJPInfo makeSJPInfo()
    {
        return abc.main.Main.v().getAbcExtension().createSJPInfo
          ("monitorenter",
           "org.aspectbench.eaj.lang.reflect.MonitorEnterSignature",
           "makeMonitorEnterSig",
           ExtendedSJPInfo.makeMonitorEnterSigData(container), stmt);
    }

    
    public String joinpointName() {
        return "lock";
    }

    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new LockShadowMatch(cim.target(), cim.map(stmt));
        cim.add(this, ret);
        return ret;
    }

}

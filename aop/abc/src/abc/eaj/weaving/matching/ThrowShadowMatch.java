/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;
import polyglot.util.InternalCompilerError;

/** The result of matching at a throw
 *  @author Julian Tibble
 */
public class ThrowShadowMatch extends StmtShadowMatch
{
    public static ShadowType shadowType()
    {
        return new ShadowType() {
            public ShadowMatch matchesAt(MethodPosition pos) {
                return ThrowShadowMatch.matchesAt(pos);
            }
        };
    }

    private Type throw_type;

    private ThrowShadowMatch(SootMethod container, Stmt stmt, Type throw_type)
    {
        super(container, stmt);
        this.throw_type = throw_type;
    }

    public Type getThrowType()
    {
        return throw_type;
    }

    public static ThrowShadowMatch matchesAt(MethodPosition pos)
    {
        if (!(pos instanceof StmtMethodPosition)) return null;
        if (abc.main.Debug.v().traceMatcher) System.err.println("Throw");

        // In Jimple: * a throw is a type of Stmt

        Stmt stmt = ((StmtMethodPosition) pos).getStmt();

        if (!(stmt instanceof ThrowStmt)) return null;   
        Type throw_type = ((ThrowStmt) stmt).getOp().getType();

        return new ThrowShadowMatch(pos.getContainer(), stmt, throw_type);
    }

    public List /*<ContextValue>*/ getArgsContextValues()
    {
        ArrayList ret = new ArrayList(1);

        Value exception = ((ThrowStmt) stmt).getOp();
        ret.add(new JimpleValue((Immediate) exception));

        return ret;
    }

    public ContextValue getTargetContextValue()
    {
        return null;
    }

    public SJPInfo makeSJPInfo()
    {
        return abc.main.Main.v().getAbcExtension().createSJPInfo
          ("throw",
           "org.aspectbench.eaj.lang.reflect.ThrowSignature",
           "makeThrowSig",
           ExtendedSJPInfo.makeThrowSigData(container, throw_type), stmt);
    }

    
    public String joinpointName() {
        return "throw";
    }

    public boolean supportsAround()
    {
        return false;
    }
    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new ThrowShadowMatch(cim.target(), cim.map(stmt), throw_type);
        cim.add(this, ret);
        return ret;
    }
}

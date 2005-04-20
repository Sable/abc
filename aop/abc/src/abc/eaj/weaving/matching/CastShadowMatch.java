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

/** The result of matching at a cast
 *  @author Julian Tibble
 */
public class CastShadowMatch extends StmtShadowMatch
{

    public static ShadowType shadowType()
    {
        return new ShadowType() {
            public ShadowMatch matchesAt(MethodPosition pos) {
                return CastShadowMatch.matchesAt(pos);
            }
        };
    }

    private Type cast_to;

    private CastShadowMatch(SootMethod container, Stmt stmt, Type cast_to)
    {
        super(container, stmt);
        this.cast_to = cast_to;
    }

    public Type getCastType()
    {
        return cast_to;
    }

    public static CastShadowMatch matchesAt(MethodPosition pos)
    {
        if (!(pos instanceof StmtMethodPosition)) return null;
        if (abc.main.Debug.v().traceMatcher) System.err.println("Cast");

        // In Jimple: * a cast can only appear as an expression
        //            * expressions are not recursive
        //            * expressions are only used as r-values
        //            * r-values only appear in assignments

        Stmt stmt = ((StmtMethodPosition) pos).getStmt();

        if (!(stmt instanceof AssignStmt)) return null;
        Value rhs = ((AssignStmt) stmt).getRightOp();

        if(!(rhs instanceof CastExpr)) return null;
        Type cast_to = ((CastExpr) rhs).getCastType();

        return new CastShadowMatch(pos.getContainer(), stmt, cast_to);
    }

    public ContextValue getReturningContextValue()
    {
        AssignStmt assign = (AssignStmt) stmt;
        return new JimpleValue((Immediate) assign.getLeftOp());
    }

    public List /*<ContextValue>*/ getArgsContextValues()
    {
        ArrayList ret = new ArrayList(1);

        CastExpr cast = (CastExpr) ((AssignStmt) stmt).getRightOp();
        ret.add(new JimpleValue((Immediate) cast.getOp()));

        return ret;
    }

    public ContextValue getTargetContextValue()
    {
        return null;
    }

    public SJPInfo makeSJPInfo()
    {
        return abc.main.Main.v().getAbcExtension().createSJPInfo
          ("cast",
           "org.aspectbench.eaj.lang.reflect.CastSignature",
           "makeCastSig",
           ExtendedSJPInfo.makeCastSigData(container, cast_to), stmt);
    }

    
    public String joinpointName() {
        return "cast";
    }

    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new CastShadowMatch(cim.target(), cim.map(stmt), cast_to);
        cim.add(this, ret);
        return ret;
    }


}

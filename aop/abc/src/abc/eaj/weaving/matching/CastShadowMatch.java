package abc.eaj.weaving.matching;

import polyglot.util.InternalCompilerError;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** The result of matching at a cast
 */
public class CastShadowMatch extends StmtShadowMatch
{
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

    public List /*<ContextValue>*/ getArgsContextValues()
    {
        ArrayList ret = new ArrayList(1);

        CastExpr cast = (CastExpr) ((AssignStmt) stmt).getRightOp();
        ret.add(new JimpleValue(cast.getOp()));

        return ret;
    }

    public ContextValue getTargetContextValue()
    {
        return null;
    }

    public SJPInfo makeSJPInfo()
    {
        throw new InternalCompilerError("Can't use joinpoint reflection " +
                        "for a cast - ajc runtime does not support it");
    }

    protected AdviceApplication doAddAdviceApplication
        (MethodAdviceList mal, AbstractAdviceDecl ad, Residue residue)
    {
        StmtAdviceApplication aa = new StmtAdviceApplication(ad,residue,stmt);
        mal.addStmtAdvice(aa);
        return aa;
    }

    public Host getHost()
    {
        return stmt;
    }
}

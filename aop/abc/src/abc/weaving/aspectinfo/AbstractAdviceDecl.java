package abc.weaving.aspectinfo;

import java.util.*;
import soot.*;
import soot.util.Chain;
import polyglot.util.Position;
import abc.weaving.matching.*;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** The base class for any kind of 'advice' declaration 
 *  @author Ganesh Sittampalam
 */
public abstract class AbstractAdviceDecl extends Syntax {
    protected AdviceSpec spec;
    protected Pointcut pc=null;

    private Pointcut origpc;
    private List formals;

    protected AbstractAdviceDecl(AdviceSpec spec,Pointcut pc,
				 List/*<Formal>*/ formals,Position pos) {
	super(pos);
	this.spec=spec;

	this.origpc=pc;
	this.formals=formals;

	if (spec instanceof AbstractAdviceSpec) {
	    ((AbstractAdviceSpec)spec).setAdvice(this);
	}
    }

    public AdviceSpec getAdviceSpec() {
	return spec;
    }

    public Pointcut getPointcut() {
	if(pc==null) pc=Pointcut.normalize(origpc,formals);
	return pc;
    }

    public abstract void debugInfo(String prefix,StringBuffer sb);

    public abstract WeavingEnv getWeavingEnv();

    public abstract WeavingContext makeWeavingContext();

    // All this JoinPoint stuff ought to move to a residue or something.
    public boolean hasJoinPoint() {
	return false;
    }

    public boolean hasJoinPointStaticPart() {
	return false;
    }

    public boolean hasEnclosingJoinPoint() {
	return false;
    }

    public abstract Chain makeAdviceExecutionStmts
	(AdviceApplication adviceappl,
	 LocalGeneratorEx localgen,WeavingContext wc);

    private int applcount=0; // the number of times this AdviceDecl matches
                             //   (i.e. the number of static join points)
    /** Increment the number of times this advice is applied, and return
     *  incremented value.
     */
    public int incrApplCount() {
        applcount++;
	return(applcount);
    }
}

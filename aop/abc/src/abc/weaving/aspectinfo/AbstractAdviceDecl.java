package abc.weaving.aspectinfo;

import java.util.*;
import soot.*;
import soot.util.Chain;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
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

	this(spec,pc,formals,pos,false);
    }

    protected AbstractAdviceDecl(AdviceSpec spec,Pointcut pc,
				 List/*<Formal>*/ formals,Position pos,
				 boolean normalized) {
	super(pos);
	this.spec=spec;

	if(normalized) this.pc=pc; else this.origpc=pc;
	this.formals=formals;

	if (spec instanceof AbstractAdviceSpec) {
	    ((AbstractAdviceSpec)spec).setAdvice(this);
	}
    }

    public AdviceSpec getAdviceSpec() {
	return spec;
    }

    // return the associated aspect, if there is one;
    // be careful calling it, since some things don't have 
    // aspects
    protected Aspect getAspect() {
	throw new InternalCompilerError("This kind of advice doesn't have an aspect: "+getClass());
    }

    public void preprocess() {
	if(pc!=null) throw new InternalCompilerError
			 ("Trying to call preprocess on an already normalized advice decl");
	pc=Pointcut.normalize(origpc,formals,getAspect());
    }

    public Pointcut getPointcut() {
	if(pc==null) throw new InternalCompilerError
			 ("Must call preprocess on advice decls before using them");
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

    public Residue preResidue(ShadowMatch sm) {
	return AlwaysMatch.v;
    }

    public Residue postResidue(ShadowMatch sm) {
	return AlwaysMatch.v;
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

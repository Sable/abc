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
	if(abc.main.Debug.v.debugPointcutNormalization) 
	    System.out.println("made unnormalized decl");
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
			 ("Trying to call preprocess on an already normalized advice decl "+this);
	if(abc.main.Debug.v.debugPointcutNormalization) System.out.println("done");
	pc=Pointcut.normalize(origpc,formals,getAspect());
	if(abc.main.Debug.v.debugPointcutNormalization) System.out.println("done");
    }

    public Pointcut getPointcut() {
	if(pc==null) throw new InternalCompilerError
			 ("Must call preprocess on advice decls before using them");
	return pc;
    }

    public List/*<Formal>*/ getFormals() {
	return formals;
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

    // for backwards compatibility, remove when aroundweaver doesn't need it
    public final Chain makeAdviceExecutionStmts
	(AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc) {
	return makeAdviceExecutionStmts(localgen,wc);
    }

    public abstract Chain makeAdviceExecutionStmts
	(LocalGeneratorEx localgen,WeavingContext wc);

    private int applcount=0; // the number of times this AdviceDecl matches
                             //   (i.e. the number of static join points)
    /** Increment the number of times this advice is applied, and return
     *  incremented value.
     */
    public int incrApplCount() {
        applcount++;
	return(applcount);
    }

    /** Get the precedence relationship between two aspects.
     *  @param a the first advice decl.
     *  @param b the second advice decl.
     *  @return
     *    {@link GlobalAspectInfo.PRECEDENCE_NONE} if none of the advice decls have precedence,
     *    {@link GlobalAspectInfo.PRECEDENCE_FIRST} if the first advice decl has precedence,
     *    {@link GlobalAspectInfo.PRECEDENCE_SECOND} if the second advice decl has precedence, or
     *    {@link GlobalAspectInfo.PRECEDENCE_CONFLICT} if there is a precedence 
     *     conflict between the two advice decls.
     */
    public static int getPrecedence(AbstractAdviceDecl a,AbstractAdviceDecl b) {
	
	// a quick first pass to assist in separating out the major classes of advice
	// consider delegating this
	int aprec=getPrecNum(a),bprec=getPrecNum(b);
	if(aprec<bprec) return GlobalAspectInfo.PRECEDENCE_FIRST;
	if(aprec>bprec) return GlobalAspectInfo.PRECEDENCE_SECOND;

	// FIXME : what happens when we merge cflow stacks?
	int aspectcomp=GlobalAspectInfo.v().getPrecedence(a.getAspect(),b.getAspect());
	if(aspectcomp!=GlobalAspectInfo.PRECEDENCE_NONE || a.getAspect()!=b.getAspect()) 
	    return aspectcomp;

	// Must be both AdviceDecl or both CflowSetup, from the same aspect
	// Check carefully just to be on the safe side
	if(a instanceof AdviceDecl && b instanceof AdviceDecl)
	    return AdviceDecl.getPrecedence((AdviceDecl) a,(AdviceDecl) b);
	if(a instanceof CflowSetup && b instanceof CflowSetup) 
	    return CflowSetup.getPrecedence((CflowSetup) a,(CflowSetup) b);

	throw new InternalCompilerError
	    ("case not handled when comparing "+a.getClass()+" and "+b.getClass());
    }
    private static int getPrecNum(AbstractAdviceDecl d) {
	if(d instanceof PerCflowSetup) return ((PerCflowSetup) d).isBelow()? 4 : 0;
	else if(d instanceof CflowSetup) return ((CflowSetup) d).isBelow() ? 3 : 1;
	else if(d instanceof PerThisSetup) return 0;
	else if(d instanceof PerTargetSetup) return 0;
	else if(d instanceof AdviceDecl) return 2;
	else throw new InternalCompilerError("Advice type not handled: "+d.getClass(),
					     d.getPosition());
    }

}

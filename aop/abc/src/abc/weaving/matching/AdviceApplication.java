package abc.weaving.matching;

import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import abc.soot.util.InPreinitializationTag;
import abc.soot.util.Restructure;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;
import java.util.*;

/** The data structure the pointcut matcher computes */
/*  @author Ganesh Sittampalam                       */
/*  @date 23-Apr-04                                  */
public abstract class AdviceApplication {

    /** The advice to be applied. If null, indicates 'dummy' advice, currently just used for
     *  thisEnclosingJoinPointStaticPart hook points.
     */
    public AbstractAdviceDecl advice;

    /** The dynamic residue */
    private ResidueBox residueBox = new ResidueBox();

    public Residue getResidue() { return residueBox.getResidue(); }
    public void setResidue(Residue r) { residueBox.setResidue(r); }

    public List/*ResidueBox*/ getResidueBoxes() {
        List/*ResidueBox*/ret = new ArrayList();
        ret.add(residueBox);
        ret.addAll(residueBox.getResidue().getResidueBoxes());
        return ret;
    }

    public ShadowMatch shadowmatch=null;
    
    public final void setShadowMatch(ShadowMatch sm) {
	shadowmatch=sm;
    }

    public AdviceApplication(AbstractAdviceDecl advice,Residue residue) {
	this.advice=advice;
	this.setResidue(residue);
    }

    /** Add some information about the advice application to a string
     *  buffer, starting each line with the given prefix
     */
    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"advice decl:\n");
       	advice.debugInfo(prefix+" ",sb);
	sb.append(prefix+"residue: "+residueBox+"\n");
	sb.append(prefix+"---"+"\n");
    }

    private static void doShadows(GlobalAspectInfo info,
				  MethodAdviceList mal,
				  SootClass cls,
				  SootMethod method,
				  MethodPosition pos) 
	throws SemanticException 
    {
	Iterator shadowIt;
	for(shadowIt=ShadowType.shadowTypesIterator();
	    shadowIt.hasNext();) {
	    
	    ShadowType st=(ShadowType) shadowIt.next();
	    ShadowMatch sm=st.matchesAt(pos);
	    
	    if(sm!=null) {

		Iterator adviceIt;
		for(adviceIt=info.getAdviceDecls().iterator();
		    adviceIt.hasNext();) {
		    final AbstractAdviceDecl ad = (AbstractAdviceDecl) adviceIt.next();

	    	    Pointcut pc=ad.getPointcut();
		    WeavingEnv we=ad.getWeavingEnv();

		    if(abc.main.Debug.v().showPointcutMatching)
			System.out.println("Matching "+pc+" at "+sm);

		    // FIXME: remove the null check once everything is properly 
		    // implemented
		    if(pc!=null) {
			// manual short-circuit logic
			Residue residue=AlwaysMatch.v;

			if(!NeverMatch.neverMatches(residue))
			    residue=AndResidue.construct
				(residue,ad.preResidue(sm));

			if(!NeverMatch.neverMatches(residue))
			    residue=AndResidue.construct
				(residue,pc.matchesAt(we,cls,method,sm));

			if(!NeverMatch.neverMatches(residue))
			    residue=AndResidue.construct
				(residue,ad.postResidue(sm));

			// Mostly this is just to eliminate advice at shadow points
			// where it can't apply - e.g. after advice at handlers
			// ajc gives a warning if we throw away a match here; 
			// we probably should too. (FIXME)
			// In the case of AfterReturningArg it does generate a real 
			// residue, but this may go away if we put the return value
			// in the shadowpoints.
			// Note that since the AdviceSpec for DeclareMessage is null,
			// this needs to come after the postResidue above. This will
			// probably change in future.

			if(!NeverMatch.neverMatches(residue))
			    residue=AndResidue.construct
				(residue,ad.getAdviceSpec().matchesAt(we,sm));

			if(abc.main.Debug.v().showPointcutMatching
			   && !NeverMatch.neverMatches(residue)) 
			    System.out.println("residue: "+residue);
			
			if(!NeverMatch.neverMatches(residue))
			    sm.addAdviceApplication(mal,ad,residue);
			
		    } else {
			if(abc.main.Debug.v().matcherWarnUnimplemented)
			    System.err.println("Got a null pointcut");
		    }
		}
	    }
	    mal.flush();
	}
    }

    public static void doMethod(GlobalAspectInfo info,
				SootClass cls,
				SootMethod method,
				Hashtable ret) 
	throws SemanticException
    {

	// Restructure everything that corresponds to a 'new' in 
	// source so that object initialisation and constructor call
	// are adjacent

	// FIXME: Replace this call with one to the partial 
	// transformer;
	// Iterate through body to find "new", decide if we have a 
	// pointcut 
	// that might match it, and add the class to the list if so
	// Either that or pre-compute the list of all classes that our
	// pointcuts could match
	
	if(abc.main.Debug.v().traceMatcher)
	    System.out.println("Doing method: "+method);

	HashMap m=new HashMap();
	m.put("enabled","true");
	if(abc.main.Debug.v().restructure)
	    System.out.println("restructuring "+method);
	(new soot.jimple.toolkits.base.JimpleConstructorFolder())
	    .transform(method.getActiveBody(),"jtp.jcf",m);

	// Identify whether we're in a constructor, and if we are identify
	// the position of the 'this' or 'super' call
	if(method.getName().equals(SootMethod.constructorName)) {
	    Stmt thisOrSuper;
	    try {
		thisOrSuper=Restructure.findInitStmt(method.getActiveBody().getUnits());
	    } catch(RuntimeException e) {
		System.err.println("Method was "+method);
		throw e;
	    }

	    Iterator stmtsIt=method.getActiveBody().getUnits().iterator();
	    while(stmtsIt.hasNext()) {
		Stmt stmt=(Stmt) stmtsIt.next();
		if(stmt==thisOrSuper) break;
		stmt.addTag(new InPreinitializationTag());
	    }
	}
	
	MethodAdviceList mal=new MethodAdviceList();

	// Do whole body shadows
	if(MethodCategory.weaveExecution(method))
	    doShadows(info,mal,cls,method,new WholeMethodPosition(method));
	
	// Do statement shadows
	if(abc.main.Debug.v().traceMatcher) 
	    System.err.println("Doing statement shadows");
	if(MethodCategory.weaveInside(method)) {
	    Chain stmtsChain=method.getActiveBody().getUnits();
	    Stmt current,next;
	    
	    if(!stmtsChain.isEmpty()) { // I guess this is actually never going to be false
		for(current=(Stmt) stmtsChain.getFirst();
		    current!=null;
		    current=next) {
		    if(abc.main.Debug.v().traceMatcher)
			System.err.println("Stmt = "+current);
		    next=(Stmt) stmtsChain.getSuccOf(current);
		    doShadows(info,mal,cls,method,new StmtMethodPosition(method,current));
		    doShadows(info,mal,cls,method,new NewStmtMethodPosition(method,current,next));
		}
	    }
	}
	
	// Do exception handler shadows

	if(abc.main.Debug.v().traceMatcher) 
	    System.err.println("Doing exception shadows");

	Chain trapsChain=method.getActiveBody().getTraps();
	Trap currentTrap;
	
	if(!trapsChain.isEmpty()) {
	    for(currentTrap=(Trap) trapsChain.getFirst();
		currentTrap!=null;
		currentTrap=(Trap) trapsChain.getSuccOf(currentTrap))
		
		doShadows(info,mal,cls,method,new TrapMethodPosition(method,currentTrap));
	}


	
	ret.put(method,mal);
    }

    public static Hashtable computeAdviceLists(GlobalAspectInfo info)
	throws SemanticException
    {
	Iterator clsIt;

	Hashtable ret=new Hashtable();

	for(clsIt=info.getWeavableClasses().iterator();clsIt.hasNext();) {

	    final AbcClass cls 
		= (AbcClass) clsIt.next();

	    SootClass sootCls = cls.getSootClass();
	    Iterator methodIt;

	    boolean hasclinit=false;

	    for(methodIt=sootCls.methodIterator();methodIt.hasNext();) {

		final SootMethod method = (SootMethod) methodIt.next();
		if(method.getName().equals(SootMethod.staticInitializerName))
		    hasclinit=true;

		if(method.isAbstract()) continue;
		if(method.isNative()) continue;

		doMethod(info,sootCls,method,ret);
	    }

	    if(!hasclinit) {
	      // System.out.println("Don't have a clinit");
		// System.out.println("Inserting " + SootMethod.staticInitializerName);
		SootMethod clinit = new SootMethod
		    (SootMethod.staticInitializerName,
		     new ArrayList(),
		     VoidType.v(),
		     Modifier.STATIC);
		sootCls.addMethod(clinit);
		Body b = Jimple.v().newBody(clinit);
		clinit.setActiveBody(b);
		b.getUnits().addLast( Jimple.v().newReturnVoidStmt() );

		doMethod(info,sootCls,clinit,ret);
	    }
	}
	return ret;
    }
}

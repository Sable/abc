package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.Host;

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
     *  thisEnclosingJoinPointStaticPart hook points. The hierarchy needs a bit of
     *  restructuring to allow for cflow stack setup advice etc.
     */
    public AdviceDecl advice;

    /** The dynamic residue */
    public Residue residue;

    public static class SJPInfo {
	public String kind;            // first parameter to makeSJP
	public String signatureTypeClass; // type returned by call following 
	public String signatureType;   // name of method to call for second parameter
        public String signature;       // parameter for call in second parameter
        public int row;                // row
	public int col;                // col

        /** the SootField corresponding to a static join point */
        public SootField sjpfield;

	SJPInfo(String kind,String signatureTypeClass,
	    String signatureType,String signature,Host host) {
	    this.kind=kind;
	    this.signatureTypeClass=signatureTypeClass;
	    this.signatureType=signatureType;
	    this.signature=signature;
	    if(host!=null && host.hasTag("SourceLnPosTag")) {
		SourceLnPosTag slpTag=(SourceLnPosTag) host.getTag("SourceLnPosTag");
		this.row=slpTag.startLn();
		this.col=slpTag.startPos();
	    } else {
		this.row=-1;
		this.col=-1;
	    }
	}

	public String toString() {
	    return kind+" "+signatureType+" "+signature+" "+row+" "+col+
	           "  "+sjpfield;
	}
    }

    /** information for generating the SJP - will be null if we don't need it */
    public SJPInfo sjpInfo;

    /** The enclosing SJP - will be null if we don't need it */
    public SJPInfo sjpEnclosing;

    /** where we store the begin and end points for weaving */
    public ShadowPoints shadowpoints; // added by LJH to keep track of
                                      // where to weave.  Is initialized
                                      // in first pass of weaver. 

    public AdviceApplication(AdviceDecl advice,Residue residue) {
	this.advice=advice;
	this.residue=residue;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"advice decl:\n");
       	if(advice!=null) advice.debugInfo(prefix+" ",sb);
	sb.append(prefix+"residue: "+residue+"\n");
	sb.append(prefix+"SJP info: "+sjpInfo+"\n");
	sb.append(prefix+"enclosing SJP info: "+sjpEnclosing+"\n");
	sb.append(prefix+"---"+"\n");
    }

    private static void doShadows(GlobalAspectInfo info,
				  MethodAdviceList mal,
				  SootClass cls,
				  SootMethod method,
				  MethodPosition pos) {
	
	Iterator shadowIt;
	for(shadowIt=ShadowType.shadowTypesIterator();
	    shadowIt.hasNext();) {
	    
	    ShadowType st=(ShadowType) shadowIt.next();
	    ShadowMatch sm=st.matchesAt(pos);
	    
	    if(sm!=null) {

		Iterator adviceIt;
		for(adviceIt=info.getAdviceDecls().iterator();
		    adviceIt.hasNext();) {
		    final AdviceDecl ad = (AdviceDecl) adviceIt.next();
		    // cache this in the AdviceDecl
		    WeavingEnv we=new AdviceFormals(ad);
	    
		    Pointcut pc=ad.getPointcut();
	    
		    // remove the null check once everything is properly 
		    // implemented
		    if(pc!=null) {
			Residue residue=pc.matchesAt(we,cls,method,sm);

			// manual short-circuit logic
			if(!NeverMatch.neverMatches(residue))
			    residue=AndResidue.construct
				(residue,ad.getAdviceSpec().matchesAt(we,sm));

			if(false) 
			    System.out.println("residue: "+residue);
			
			if(!NeverMatch.neverMatches(residue))
			    sm.addAdviceApplication(mal,ad,residue);
			
		    } else {
			if(abc.main.Debug.v.matcherWarnUnimplemented)
			    System.err.println("Got a null pointcut");
		    }
		}
	    }
	}
    }

    public static void doMethod(GlobalAspectInfo info,
				SootClass cls,
				SootMethod method,
				Hashtable ret) {

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
	
	HashMap m=new HashMap();
	m.put("enabled","true");
	(new soot.jimple.toolkits.base.JimpleConstructorFolder())
	    .transform(method.getActiveBody(),"jtp.jcf",m);

	// Identify whether we're in a constructor, and if we are identify
	// the position of the 'this' or 'super' call
	if(method.getName().equals(SootMethod.constructorName)) {
	    Stmt thisOrSuper=
	      Restructure.findInitStmt(method.getActiveBody().getUnits());

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
	if(MethodCategory.weaveInside(method)) {
	    Chain stmtsChain=method.getActiveBody().getUnits();
	    Stmt current,next;
	    
	    if(!stmtsChain.isEmpty()) { // I guess this is actually never going to be false
		for(current=(Stmt) stmtsChain.getFirst();
		    current!=null;
		    current=next) {
		    
		    next=(Stmt) stmtsChain.getSuccOf(current);
		    doShadows(info,mal,cls,method,new StmtMethodPosition(method,current));
		    doShadows(info,mal,cls,method,new NewStmtMethodPosition(method,current,next));
		}
	    }
	}
	
	// Do exception handler shadows
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

    public static Hashtable computeAdviceLists(GlobalAspectInfo info) {
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

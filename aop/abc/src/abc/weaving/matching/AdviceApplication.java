package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;
import java.util.*;

/** The data structure the pointcut matcher computes */
/*  @author Ganesh Sittampalam                       */
/*  @date 23-Apr-04                                  */
public abstract class AdviceApplication {
    public AdviceDecl advice;
    public Residue residue;

    public static class SJPInfo {
	public String kind;            // first parameter to makeSJP
	public String signatureType;   // name of method to call for second parameter
        public String signature;       // parameter for call in second parameter
        public int row;                // row
	public int col;                // col

	// Where do we get this stuff from? 
	// The kind comes from the shadow type.
	// so does the signature type
	// The shadow match presumably needs to pick up the signature.
	// Hopefully it can get the line/column information too.

	SJPInfo(String kind,String signatureType,String signature,int row,int col) {
	    this.kind=kind;
	    this.signatureType=signatureType;
	    this.signature=signature;
	    this.row=row;
	    this.col=col;
	}
    };

    public SJPInfo sjpInfo;
	

    public ShadowPoints shadowpoints; // added by LJH to keep track of
                                      // where to weave.  Is initialized
                                      // in first pass of weaver. 

    public AdviceApplication(AdviceDecl advice,Residue residue,SJPInfo sjpInfo) {
	this.advice=advice;
	this.residue=residue;
	this.sjpInfo=sjpInfo;
    }

    private static void doStatement(GlobalAspectInfo info,
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
		    WeavingEnv we=new AdviceFormals(ad);
	    
		    Pointcut pc=ad.getPointcut();
	    
		    // remove the null check once everything is properly 
		    // implemented
		    if(pc!=null) {
			Residue residue=pc.matchesAt(we,cls,method,sm);
			
			if(false) 
			    System.out.println("residue: "+residue);
			
			if(!NeverMatch.neverMatches(residue))
			    sm.addAdviceApplication(mal,ad,residue);
			
		    }
		}
	    }
	}
    }

    public static void doMethod(GlobalAspectInfo info,
				SootClass cls,
				SootMethod method,
				Hashtable ret) {
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
	
	MethodAdviceList mal=new MethodAdviceList();
	
	// Do whole body shadows
	doStatement(info,mal,cls,method,new WholeMethodPosition(method));
	
	// Do statement shadows
	Chain stmtsChain=method.getActiveBody().getUnits();
	Stmt current,next;
	
	if(!stmtsChain.isEmpty()) { // I guess this is actually never going to be false
	    for(current=(Stmt) stmtsChain.getFirst();
		current!=null;
		current=next) {
		next=(Stmt) stmtsChain.getSuccOf(current);
		doStatement(info,mal,cls,method,new StmtMethodPosition(current));
		doStatement(info,mal,cls,method,new NewStmtMethodPosition(current,next));
	    }
	}
	
	// Do exception handler shadows
	Chain trapsChain=method.getActiveBody().getTraps();
	Trap currentTrap;
	
	if(!trapsChain.isEmpty()) {
	    for(currentTrap=(Trap) trapsChain.getFirst();
		currentTrap!=null;
		currentTrap=(Trap) trapsChain.getSuccOf(currentTrap))
		
		doStatement(info,mal,cls,method,new TrapMethodPosition(currentTrap));
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
	        System.out.println("Don't have a clinit");
		System.out.println("Inserting " + SootMethod.staticInitializerName);
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

package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

public class Weaver {

    public GlobalAspectInfo gai;

    public Weaver() {
	gai=new GlobalAspectInfo();
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {

            final SootClass cl = (SootClass) clIt.next();
            G.v().out.println( "Weaver constructor "+cl.toString() );
            if( isAspect(cl) ) {
                System.out.println( "it's an aspect");
		final Aspect aspect=new Aspect(new abc.weaving.aspectinfo.Class(cl.getName()),null,null);
		gai.addAspect(aspect);
		gai.addAdviceDecl(new AdviceDecl(null,new SetField(null),null,aspect,null));
            } else {
                System.out.println( "it's not an aspect");
		gai.addClass(new abc.weaving.aspectinfo.Class(cl.getName()));
            }
        }   
    }

    public void weave() {
        G.v().out.println( "The application classes are:" );

        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {

            final SootClass cl = (SootClass) clIt.next();
            G.v().out.println( "For the class "+cl.toString() );
	    if(!isAspect(cl) ) {
		System.out.println("Weave: it's not an aspect: " + cl);
		weaveInAspects( cl );
	       }
             else {
	        System.out.println("Weave: it's an aspect: " + cl);
                fillInAspect( cl ); 
	       }
	}
        G.v().out.println( "finished application classes" );
    }

    public void fillInAspect( SootClass cl ) {
        System.out.println( "filling in aspect "+cl );

        SootField instance = new SootField( "ajc$perSingletonInstance", 
	    cl.getType(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL );
        cl.addField( instance );
        generateAspectOfBody(cl);
        generateClinitBody(cl);
    }

    private void generateAspectOfBody( SootClass cl ) {
        SootMethod aspectOf = cl.getMethodByName( "aspectOf" );
        Body b = Jimple.v().newBody(aspectOf);
        aspectOf.setActiveBody(b);

        SootClass nabe = Scene.v().getSootClass(
	                          "org.aspectj.lang.NoAspectBoundException");
        //Local rthis = Jimple.v().newLocal("rthis", cl.getType());
        Local r0 = Jimple.v().newLocal("r0", cl.getType());
        Local r1 = Jimple.v().newLocal("r1", nabe.getType() );

        b.getLocals().add(r0);
        b.getLocals().add(r1);

        StaticFieldRef ref = Jimple.v().newStaticFieldRef(cl.getFieldByName("ajc$perSingletonInstance"));

        Chain units = b.getUnits(); 

        //units.addLast( Jimple.v().newIdentityStmt( rthis, newThisRef(cl)));
        units.addLast( Jimple.v().newAssignStmt( r0, ref));
        Stmt newExceptStmt = Jimple.v().newAssignStmt( r1, Jimple.v().newNewExpr( nabe.getType() ) );
        units.addLast( Jimple.v().newIfStmt( Jimple.v().newEqExpr( r0, NullConstant.v() ), newExceptStmt ));
        units.addLast( Jimple.v().newReturnStmt( r0 ) );
        units.addLast( newExceptStmt );
        units.addLast( Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( r1, nabe.getMethod( "<init>", new ArrayList() ) ) ) ); 
        units.addLast( Jimple.v().newThrowStmt( r1 ) );
    }

    private void generateClinitBody( SootClass cl ) {
        SootMethod postClinit = new SootMethod( "ajc$postClinit", new ArrayList(), VoidType.v(), Modifier.PRIVATE | Modifier.STATIC );
        cl.addMethod( postClinit );
        Body b = Jimple.v().newBody(postClinit);
        postClinit.setActiveBody(b);

        Local r0 = Jimple.v().newLocal("r0", cl.getType());
        b.getLocals().add(r0);
	System.out.println("getting clinit");

        Chain units = b.getUnits();
        units.addLast( Jimple.v().newAssignStmt( r0, Jimple.v().newNewExpr( cl.getType() ) ) );
        units.addLast( Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( r0, cl.getMethod( "<init>", new ArrayList() ) ) ) );
        StaticFieldRef ref = Jimple.v().newStaticFieldRef(cl.getFieldByName("ajc$perSingletonInstance"));
        units.addLast( Jimple.v().newAssignStmt( ref, r0 ) );
        units.addLast( Jimple.v().newReturnVoidStmt() ); 

        SootMethod clinit;

        if( !cl.declaresMethod( "void <clinit>()" ) ) {
	    System.out.println("There is no clinit, must build one");
            clinit = new SootMethod( "<clinit>", new ArrayList(), VoidType.v(), Modifier.STATIC );
            cl.addMethod( clinit );
            b = Jimple.v().newBody(clinit);
            clinit.setActiveBody(b);
            b.getUnits().addLast( Jimple.v().newReturnVoidStmt() );
        }

	System.out.println("getting clinit");
        clinit = cl.getMethod("void <clinit>()");

        units = clinit.retrieveActiveBody().getUnits();
        Iterator it = units.snapshotIterator();
        while( it.hasNext() ) {
            Stmt s = (Stmt) it.next();
            if( s instanceof ReturnVoidStmt ) {
                units.insertBefore( 
                        Jimple.v().newInvokeStmt( Jimple.v().newStaticInvokeExpr( postClinit ) ),
                        s );
            }
        }
    }

    public void weaveInAspects( SootClass cl ) {
        for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
            final SootMethod method = (SootMethod) methodIt.next();
            if( method.isAbstract() ) continue;
            if( method.isNative() ) continue;

	    List/*<AdviceApplication>*/ adviceList = gai.getAdviceList(method);
	    System.out.println("AdviceList for " + method );
	    System.out.println(adviceList.toString());

            Body b = method.getActiveBody();
            Chain units = b.getUnits();
            Iterator codeIt = units.snapshotIterator();
	    Iterator adviceIt = adviceList.iterator();
	    Stmt stmt = null;
	    AdviceApplication aa = null;
            while( codeIt.hasNext() && adviceIt.hasNext()) {
		if(stmt==null) stmt = (Stmt) codeIt.next();
		if(aa==null) aa = (AdviceApplication) adviceIt.next();
		if(stmt!=aa.begin) {
		    stmt=null;
		    continue;
		}
		final AdviceDecl advicedecl=aa.advice;
		final SootClass aspect=advicedecl.getAspect().getInstanceClass().getSootClass();

		Local l = Jimple.v().newLocal( localName(), aspect.getType() );
		b.getLocals().add(l);
		units.insertBefore( Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList()))), stmt);
		units.insertBefore( 
				   Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr( l, aspect.getMethod("before$1", new ArrayList() ) ) ), stmt );
		aa=null;
	    }
	}
    }

    private static boolean isAspect( SootClass cl ) {
        if( cl.getName().equals( "Aspect" ) ) return true;
        return false;
    }
    static int localNum = 1;
    private static String localName() {
        localNum++;
        return "aspect$"+localNum;
    }
}

package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

public class AspectGenerator {

    public void fillInAspect( SootClass cl ) {
        System.out.println( "filling in aspect "+cl );

        SootField instance = new SootField( "ajc$perSingletonInstance", 
	    cl.getType(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL );
        cl.addField( instance );
        generateAspectOfBody(cl);
        generateClinitBody(cl);
        generateHasAspectBody(cl);
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

    private void generateHasAspectBody(SootClass cl){
        SootMethod hasAspect;
        if (!cl.declaresMethod("boolean hasAspect()")){
            hasAspect = new SootMethod("hasAspect", new ArrayList(), BooleanType.v(), Modifier.PUBLIC | Modifier.STATIC);
            cl.addMethod(hasAspect);
        }
        else {
            hasAspect = cl.getMethodByName("hasAspect");
        }

        Body b = Jimple.v().newBody(hasAspect);
        hasAspect.setActiveBody(b);

        LocalGenerator lg = new LocalGenerator(b);
        Local r0 = lg.generateLocal(cl.getType());
        
        StaticFieldRef ref = Jimple.v().newStaticFieldRef(cl.getFieldByName("ajc$perSingletonInstance"));
        
        Chain units = b.getUnits(); 
        
        units.addLast( Jimple.v().newAssignStmt( r0, ref));
        ReturnStmt ret0 = Jimple.v().newReturnStmt( IntConstant.v(0) );

        units.addLast( Jimple.v().newIfStmt( Jimple.v().newEqExpr( r0, NullConstant.v() ), ret0 ));
        
        units.addLast( Jimple.v().newReturnStmt( IntConstant.v(1) ) );

        units.addLast( ret0);
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

}

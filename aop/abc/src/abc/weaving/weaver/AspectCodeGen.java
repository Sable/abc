package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;

import abc.soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;


/** Adds fields and methods to classes reprsenting aspects.
 *   @author Jennifer Lhotak 
 *   @author Ondrej Lhotak
 *   @author Laurie Hendren
 *   @date April 30, 2004 
 */

public class AspectCodeGen {

      private static void debug(String message)
        { if (abc.main.Debug.v().aspectCodeGen) 
	    System.err.println("ACG*** " + message); }


    /** top-level call to fill in aspect with fields and methods */
    public void fillInAspect( SootClass cl ) {
        debug("--- BEGIN filling in aspect "+cl );

	// add public static final ajc$perSingletonInstance field
	debug(" ... adding ajc$perSingletonInstance");
        SootField instance = new SootField( "ajc$perSingletonInstance", 
	    cl.getType(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL );
        cl.addField( instance );

	// add private static java.lang.Throwable ajc$initFailureCause field
	debug(" ... adding ajc$intFailureCause");
        SootField instance2 = new SootField( "ajc$initFailureCause", 
	    RefType.v("java.lang.Throwable"),
	    Modifier.PRIVATE | Modifier.STATIC );
        cl.addField( instance2 );

	// front end has put aspectOf method, fill in body
	debug(" ... adding aspectOf body");
        generateAspectOfBody(cl);

	// front end has put hasAspect method, fill in body
	debug(" ... adding hasAspect body");
        generateHasAspectBody(cl);

	// add appropriate init code to clinit(), create one if none exists
	debug(" ... handling clinit()");
        generateClinitBody(cl);

        debug( "--- END filling in aspect "+cl +"\n" );
    }

    /** front-end has already generated empty aspectOf body,
     *    this method fills in body with implementation
     */
    private void generateAspectOfBody( SootClass cl ) {
        // get the body of aspectOf
        SootMethod aspectOf = cl.getMethodByName( "aspectOf" );
        Body b = Jimple.v().newBody(aspectOf);
        aspectOf.setActiveBody(b);

	// get the class for org.aspectj.lang.noAspectBoundException
        SootClass nabe = Scene.v().getSootClass(
	                          "org.aspectj.lang.NoAspectBoundException");
        Local theAspect = Jimple.v().newLocal("theAspect", cl.getType());
        Local nabException = Jimple.v().newLocal("nabException", 
	                                         nabe.getType() );
	Local failureCause = Jimple.v().
	  newLocal("failureCause", RefType.v("java.lang.Throwable"));

	// add locals:   <AspectType> theAspect; 
	//               org.aspectj.lang.NoAspectBoundException nabException; 
	//               java.lang.Throwable failurecause; 
        b.getLocals().add(theAspect);
        b.getLocals().add(nabException);
        b.getLocals().add(failureCause);

	// make a field ref to static field ajc$perSingletonInstance
        StaticFieldRef ref = Jimple.v().
	    newStaticFieldRef(cl.getFieldByName("ajc$perSingletonInstance"));

	// get the units of the body so we can insert new Jimple stmts
        Chain units = b.getUnits(); 

        units.addLast( Jimple.v().newAssignStmt( theAspect, ref));
        Stmt newExceptStmt = Jimple.v().newAssignStmt( nabException, 
	                     Jimple.v().newNewExpr( nabe.getType() ) );
        units.addLast( Jimple.v().newIfStmt( Jimple.v().
	          newEqExpr( theAspect, NullConstant.v() ), newExceptStmt ));
        units.addLast( Jimple.v().newReturnStmt( theAspect ) );
        units.addLast( newExceptStmt );
	List typelist = new LinkedList();
	typelist.add(RefType.v("java.lang.String"));
	typelist.add(RefType.v("java.lang.Throwable"));
	SootMethod initthrowmethod = nabe.getMethod("<init>",typelist);
	debug("init method for the throw in aspectOf is " + initthrowmethod);
	StaticFieldRef causefield = 
	  Jimple.v().
	    newStaticFieldRef(cl.getFieldByName("ajc$initFailureCause"));
	Stmt assigntocause =
	   Jimple.v().
	     newAssignStmt(failureCause,causefield);
	units.addLast(assigntocause);
	List arglist = new LinkedList();
        // string constant with name of aspect
	arglist.add(StringConstant.v(cl.getName())); 
	// local pointing to cause
	arglist.add(failureCause);  
	// get the cause instance
	Stmt exceptioninit = 
	   Jimple.v().
	     newInvokeStmt( Jimple.v().newSpecialInvokeExpr
		 ( nabException, initthrowmethod, arglist) ) ; 
        units.addLast( exceptioninit );
        units.addLast( Jimple.v().newThrowStmt( nabException ) );

	// have generated:
	//    theAspect = <AspectType>.ajc$perSingletonInstance;
	//    if (theAspect == null) goto newExceptStmt;
	//    return(theAspect)
	//    newExceptStmt: nabException = 
	//                  new org.aspectj.lang.noAspectBoundException
	//    failureCause = ajc$initFailureCause
	//    org.aspectj.lang.noAspectBoundException.nabException.<init>
	//                  ("AspectName",failureCause)
	//    throw nabException 
    }


    /** front-end has already generated empty hasAspect body,
     *    this method fills in body with implementation
     */
    private void generateHasAspectBody(SootClass cl){
        // get body
        SootMethod hasAspect;
        hasAspect = cl.getMethodByName("hasAspect");
        Body b = Jimple.v().newBody(hasAspect);
        hasAspect.setActiveBody(b);

	// make a LocalGenerator (will give new local names)
        LocalGeneratorEx lg = new LocalGeneratorEx(b);

	// make a local,   <AspectType> theAspect;
        Local theAspect = lg.generateLocal(cl.getType(),"theAspect");
        
	// make a static ref,  <AspectType>.ajc$PerSingletonInstance
        StaticFieldRef ref = Jimple.v().
	      newStaticFieldRef(cl.getFieldByName("ajc$perSingletonInstance"));
        
	// get a Chain of Jimple stmts so new stmts can be inserted
        Chain units = b.getUnits(); 
        
        units.addLast( Jimple.v().newAssignStmt( theAspect, ref));
        ReturnStmt ret0 = Jimple.v().newReturnStmt( IntConstant.v(0) );

        units.addLast( Jimple.v().newIfStmt( Jimple.v().newEqExpr( theAspect, 
		          NullConstant.v() ), ret0 ));
        
        units.addLast( Jimple.v().newReturnStmt( IntConstant.v(1) ) );

        units.addLast( ret0);
	// have generated:
	//    theAspect = <AspectType>.ajc$PerSingleonInstance
	//    if (theAspect == null) goto newReturnStmt
	//    return(1)
	//    newReturnStmt: return(0)
    }
    
    /** Create a new method postClinit(). 
     *
     *  If Clinit() already exists, add call to postClinit at end, 
     *  otherwise create a new Clinit and add statements to it.
     */ 
    private void generateClinitBody( SootClass cl ) {

        // create method:
        //      private static void ajc$postClinit() {};
        SootMethod postClinit = new SootMethod( "ajc$postClinit", 
	    new ArrayList(), VoidType.v(), Modifier.PRIVATE | Modifier.STATIC );

	// add it to the aspect class
        cl.addMethod( postClinit );

	// make a new body and set it as the active one
        Body b = Jimple.v().newBody(postClinit);
        postClinit.setActiveBody(b);

	// create local:   <AspectType> theAspect;
        Local theAspect = Jimple.v().newLocal("theAspect", cl.getType());
        b.getLocals().add(theAspect);

	// get the chain of Jimple statments for the body
        Chain units = b.getUnits();
        units.addLast( Jimple.v().
	  newAssignStmt( theAspect, Jimple.v().newNewExpr( cl.getType() ) ) );
        units.addLast( Jimple.v().
	  newInvokeStmt( Jimple.v().
	    newSpecialInvokeExpr( theAspect, 
	      cl.getMethod( "<init>", new ArrayList() ) ) ) );
        StaticFieldRef ref = Jimple.v().
	    newStaticFieldRef(cl.getFieldByName("ajc$perSingletonInstance"));
        units.addLast( Jimple.v().newAssignStmt( ref, theAspect ) );
        units.addLast( Jimple.v().newReturnVoidStmt() ); 

	// have generated the body:
	//    theAspect = new <AspectType> ();
	//    theAspect.<init>();
	//    <AspectType>.ajc$perSingletonInstance = theAspect;
	//    return;

	// now put call to ajc$postClinit() into body of Clinit()
        SootMethod clinit;

	// if there is no clinit method already, create one
        if( !cl.declaresMethod( "void <clinit>()" ) ) 
	   throw new CodeGenException("should always be one here");

	   /*
	   {
	    debug("There is no clinit, must build one");
            clinit = new SootMethod( "<clinit>", new ArrayList(), VoidType.v(), Modifier.STATIC );
            cl.addMethod( clinit );
            b = Jimple.v().newBody(clinit);
            clinit.setActiveBody(b);
            b.getUnits().addLast( Jimple.v().newReturnVoidStmt() );
            }
	    */

	debug("getting clinit");
        clinit = cl.getMethod("void <clinit>()");
        // get the body
	Body b2 = clinit.retrieveActiveBody();
	// then the units
        units = b2.getUnits();
        // get a local generator for the body
	LocalGeneratorEx localgen = new LocalGeneratorEx(b2);
	// need a snapshotIterator because we are modifying units as we
	// traverse it
        Iterator it = units.snapshotIterator();
        while( it.hasNext() ) {
            Stmt s = (Stmt) it.next();
	    // insert a call to postClinit() just before each return
            if( s instanceof ReturnVoidStmt ) 
	      { // make a nop stmt which we will goto
		debug("LJH - inserting before return");
	        Stmt nop = Jimple.v().newNopStmt();	
		// postClinit(); 
		Stmt invokepostClinit =
                   Jimple.v().
		     newInvokeStmt( 
			 Jimple.v().newStaticInvokeExpr( postClinit ) );
		units.insertBefore(invokepostClinit,s);
		// goto return;
		Stmt goto_s = 
		    Jimple.v().newGotoStmt(nop);
		units.insertBefore(goto_s,s);
	        // catchlocal := @caughtexception
		Local catchLocal =
		    localgen.generateLocal(RefType.v("java.lang.Throwable"),
			"catchLocal");
		CaughtExceptionRef exceptRef = 
		    Jimple.v().newCaughtExceptionRef();
		Stmt exceptionidentity =
		    Jimple.v().newIdentityStmt(catchLocal, exceptRef);
		units.insertBefore(exceptionidentity,s);
		// ajc$initFailureCause := catchlocal    
                StaticFieldRef cause = 
		    Jimple.v().
	               newStaticFieldRef(
			   cl.getFieldByName("ajc$initFailureCause"));
		Stmt assigntofield =
		    Jimple.v().
		       newAssignStmt(cause,catchLocal);
		units.insertBefore(assigntofield,s);
		// add nop before return
		units.insertBefore(nop,s);
		// add the try ... catch
                b2.getTraps().
		  add(Jimple.v().
		        newTrap(Scene.v().getSootClass("java.lang.Throwable"),
			invokepostClinit,goto_s,exceptionidentity));

		// have created:
		//    java.lang.Exception catchLocal
		//    ...
		//    invokepostClinit :  postClinit();
		//    goto_s           :  goto nop;
		//    exceptionidentity:  catchLocal := @caughtexception;
		//    assigntofield    :  ajc$initFailureCause := catchLocal;
		//    nop              :  nop
		//    S                :  return;
		//
		//    catch from invokepostClinit upto goto_s handlewith
		//                       exceptionidentity
            }
        }
    }
}

package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;

import abc.soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

/** Adds fields and methods to classes representing aspects.
 *   @author Jennifer Lhotak 
 *   @author Ondrej Lhotak
 *   @author Laurie Hendren
 *   @date April 30, 2004 
 */

public class AspectCodeGen {

    private static void debug(String message)
      { if (abc.main.Debug.v().aspectCodeGen) 
          System.err.println("ACG*** " + message); 
      }

    public void fillInAspect(Aspect aspct)
      { Per per = aspct.getPer();
        if (per instanceof Singleton) // singleton aspect 
          fillInSingletonAspect(aspct);
        else if ((per instanceof PerThis) || 
                 (per instanceof PerTarget))
          fillInPerAspect(aspct);
        else if ((per instanceof PerCflow) ||
                 (per instanceof PerCflowBelow))
          throw new CodeGenException(
                              "Can't gen code for percflow/percflowbelow");
        else
          throw new CodeGenException("Unknown kind of per aspect");
      }

    /* ===================== SINGLETON ASPECT =================== */

    /** top-level call to fill in singleton aspect with fields and methods */
    public void fillInSingletonAspect( Aspect aspct ) 
      { debug("--- BEGIN filling in Singleton aspect "+ 
            aspct.getInstanceClass().getName() );
        SootClass cl = aspct.getInstanceClass().getSootClass();

        // add public static final abc$perSingletonInstance field
        debug(" ... adding abc$perSingletonInstance");
        SootField instance = new SootField( "abc$perSingletonInstance", 
        cl.getType(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL );
        cl.addField( instance );

        // add private static java.lang.Throwable abc$initFailureCause field
        debug(" ... adding abc$intFailureCause");
        SootField instance2 = new SootField( "abc$initFailureCause", 
        RefType.v("java.lang.Throwable"),
        Modifier.PRIVATE | Modifier.STATIC );
        cl.addField( instance2 );

        // front end has put aspectOf method, fill in body
        debug(" ... adding aspectOf body");
        generateSingletonAspectOfBody(cl);

        // front end has put hasAspect method, fill in body
        debug(" ... adding hasAspect body");
        generateSingletonHasAspectBody(cl);

        // add appropriate init code to clinit()
        debug(" ... handling clinit()");
        generateSingletonClinitBody(cl);

        debug( "--- END filling in Singleton aspect "+cl +"\n" );
      }

    /** front-end has already generated empty aspectOf body,
     *    this method fills in body with implementation
     */
    private void generateSingletonAspectOfBody( SootClass cl ) {
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

      // make a field ref to static field abc$perSingletonInstance
      StaticFieldRef ref = Jimple.v().
          newStaticFieldRef(cl.getFieldByName("abc$perSingletonInstance"));

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
          newStaticFieldRef(cl.getFieldByName("abc$initFailureCause"));
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
      //    theAspect = <AspectType>.abc$perSingletonInstance;
      //    if (theAspect == null) goto newExceptStmt;
      //    return(theAspect)
      //    newExceptStmt: nabException = 
      //                  new org.aspectj.lang.noAspectBoundException
      //    failureCause = abc$initFailureCause
      //    org.aspectj.lang.noAspectBoundException.nabException.<init>
      //                  ("AspectName",failureCause)
      //    throw nabException 
    }


    /** front-end has already generated empty hasAspect body,
     *    this method fills in body with implementation
     */
    private void generateSingletonHasAspectBody(SootClass cl){
      // get body
      SootMethod hasAspect;
      hasAspect = cl.getMethodByName("hasAspect");
      Body b = Jimple.v().newBody(hasAspect);
      hasAspect.setActiveBody(b);

      // make a LocalGenerator (will give new local names)
      LocalGeneratorEx lg = new LocalGeneratorEx(b);

      // make a local,   <AspectType> theAspect;
      Local theAspect = lg.generateLocal(cl.getType(),"theAspect");
        
      // make a static ref,  <AspectType>.abc$PerSingletonInstance
      StaticFieldRef ref = Jimple.v().
          newStaticFieldRef(cl.getFieldByName("abc$perSingletonInstance"));
        
      // get a Chain of Jimple stmts so new stmts can be inserted
      Chain units = b.getUnits(); 
        
      units.addLast( Jimple.v().newAssignStmt( theAspect, ref));
      ReturnStmt ret0 = Jimple.v().newReturnStmt( IntConstant.v(0) );

      units.addLast( Jimple.v().newIfStmt( Jimple.v().newEqExpr( theAspect, 
                  NullConstant.v() ), ret0 ));
        
      units.addLast( Jimple.v().newReturnStmt( IntConstant.v(1) ) );

      units.addLast( ret0);
      // have generated:
      //    theAspect = <AspectType>.abc$PerSingleonInstance
      //    if (theAspect == null) goto newReturnStmt
      //    return(1)
      //    newReturnStmt: return(0)
    }
    
    /** Create a new method postClinit(). 
     *
     *  If Clinit() already exists, add call to postClinit at end, 
     *  otherwise create a new Clinit and add statements to it.
     */ 
    private void generateSingletonClinitBody( SootClass cl ) {
      // create method:
      //      private static void abc$postClinit() {};
      SootMethod postClinit = new SootMethod( "abc$postClinit", 
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
      newStaticFieldRef(cl.getFieldByName("abc$perSingletonInstance"));
      units.addLast( Jimple.v().newAssignStmt( ref, theAspect ) );
      units.addLast( Jimple.v().newReturnVoidStmt() ); 

      // have generated the body:
      //    theAspect = new <AspectType> ();
      //    theAspect.<init>();
      //    <AspectType>.abc$perSingletonInstance = theAspect;
      //    return;

      // now put call to abc$postClinit() into body of Clinit()
      SootMethod clinit;

      // there should already be a clinit body 
      if( !cl.declaresMethod( "void <clinit>()" ) ) 
        throw new CodeGenException("should always be one here");

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
             Stmt goto_s = Jimple.v().newGotoStmt(nop);
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
             // abc$initFailureCause := catchlocal    
             StaticFieldRef cause = 
                 Jimple.v().
                      newStaticFieldRef(
                         cl.getFieldByName("abc$initFailureCause"));
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
            //    assigntofield    :  abc$initFailureCause := catchLocal;
            //    nop              :  nop
            //    S                :  return;
            //
            //    catch from invokepostClinit upto goto_s handlewith
            //                       exceptionidentity
           }
        }
    }

    /* ===================== PERTHIS/PERTARGET ASPECT ========== */

    /** top-level call to fill in per aspect with fields and methods */
    public void fillInPerAspect( Aspect aspct ) 
      { SootClass cl = aspct.getInstanceClass().getSootClass();
        String aname = aspct.getInstanceClass().getName();
        String perkind = 
          aspct.getPer() instanceof PerThis ? "This" : "Target";

        debug("--- BEGIN filling in per aspect "+ aname + 
                 " with kind " + perkind);

        String perInterfaceName = aname + "$abc$Per" + perkind;
        String perGetName = perInterfaceName + "Get";
        String perSetName = perInterfaceName + "Set";

        debug("perInterfaceName is " + perInterfaceName);

        // generate the interface
        genPerInterface(aname, perInterfaceName, perGetName, perSetName);

        // front end has put aspectOf method, fill in body
        debug(" ... adding aspectOf body");
        genPerAspectOfBody(cl, aname, perInterfaceName, perGetName);

        // front end has put hasAspect method, fill in body
        debug(" ... adding hasAspect body");
        genPerHasAspectBody(cl, perInterfaceName, perGetName);

        // generate the per Object bind method
        debug("... adding per Object bind method");
        genPerObjectBindMethod(cl, perInterfaceName, perGetName, perSetName);

        debug( "--- END filling in per aspect "+ aname +
                        " with kind " + perkind +"\n" );
      }

    // make interface 
    private void genPerInterface(String aspectName, String interfaceName,
                                      String getName, String setName)
      {
    
      }

    // fill in body of <AspectName> aspectOf (java.lang.Object)
    private void genPerAspectOfBody(SootClass cl, 
                      String aspectName, String interfaceName, String getName)
      {
      }

    // fill in body of boolean hasAspect(java.lang.Object)
    private void genPerHasAspectBody(SootClass cl, 
                       String interfacename, String getName)
      {
      }
   
    // create method public static void abc$perObjectBind(java.lang.Object)
    private void genPerObjectBindMethod(SootClass cl, 
                       String interfaceName, String getName, String setName)
      {
      }
    
    /* =============== PERCFLOW/PERCFLOWBELOW ASPECT ============= */
    // aspectOf
    
    // hasAspect

    // add static for cflow stack, add initializer in preClinit()

}

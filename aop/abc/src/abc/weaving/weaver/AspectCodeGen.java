/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import polyglot.util.InternalCompilerError;

import abc.soot.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;


/** Adds fields and methods to classes representing aspects.
 *   @author Jennifer Lhotak
 *   @author Ondrej Lhotak
 *   @author Laurie Hendren
 *   @author Ganesh Sittampalam
 */

public class AspectCodeGen {

    private static void debug(String message)
      { if (abc.main.Debug.v().aspectCodeGen)
          System.err.println("ACG*** " + message);
      }

    public void fillInAspect(Aspect aspct)
      {
          if(aspct.getInstanceClass().getSootClass().isAbstract()) return;
          try {
              Per per = aspct.getPer();
              if (per instanceof Singleton) // singleton aspect
                  fillInSingletonAspect(aspct);
              else if ((per instanceof PerThis) ||
                       (per instanceof PerTarget))
                  fillInPerObjectAspect(aspct);
              else if ((per instanceof PerCflow) ||
                       (per instanceof PerCflowBelow))
                  fillInPerCflowAspect(aspct);
              else
                  throw new CodeGenException("Unknown kind of per aspect");
          } catch(InternalCompilerError e) {
              throw new InternalCompilerError(e.message()+" while filling in "+aspct,
                                              e.position(),
                                              e.getCause());
          } catch(Throwable e) {
              throw new InternalCompilerError("exception while filling in "+aspct,e);
          }
      }

    /* ===================== SINGLETON ASPECT =================== */

    /** top-level call to fill in singleton aspect with fields and methods */
    public void fillInSingletonAspect( Aspect aspct )
      { debug("--- BEGIN filling in Singleton aspect "+
            aspct.getName() );
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
      if (Modifier.isAbstract(cl.getModifiers()))
        return;
      SootMethod aspectOf = cl.getMethod("aspectOf",new ArrayList());
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
          newStaticFieldRef(Scene.v().makeFieldRef(cl,"abc$perSingletonInstance",cl.getType(),true));

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
      SootMethodRef initthrowmethod = Scene.v().makeConstructorRef(nabe,typelist);
      debug("init method for the throw in aspectOf is " + initthrowmethod);
      StaticFieldRef causefield =
        Jimple.v().
          newStaticFieldRef(Scene.v().makeFieldRef
                            (cl,"abc$initFailureCause",RefType.v("java.lang.Throwable"),true));
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
      Tagger.tagChain(units, InstructionKindTag.ASPECT_CODE);

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
      if (Modifier.isAbstract(cl.getModifiers()))
        return;
      hasAspect = cl.getMethod("hasAspect", new ArrayList());
      Body b = Jimple.v().newBody(hasAspect);
      hasAspect.setActiveBody(b);

      // make a LocalGenerator (will give new local names)
      LocalGeneratorEx lg = new LocalGeneratorEx(b);

      // make a local,   <AspectType> theAspect;
      Local theAspect = lg.generateLocal(cl.getType(),"theAspect");

      // make a static ref,  <AspectType>.abc$PerSingletonInstance
      StaticFieldRef ref = Jimple.v().
          newStaticFieldRef(Scene.v().makeFieldRef(cl,"abc$perSingletonInstance",cl.getType(),true));

      // get a Chain of Jimple stmts so new stmts can be inserted
      Chain units = b.getUnits();

      units.addLast( Jimple.v().newAssignStmt( theAspect, ref));
      ReturnStmt ret0 = Jimple.v().newReturnStmt( IntConstant.v(0) );

      units.addLast( Jimple.v().newIfStmt( Jimple.v().newEqExpr( theAspect,
                  NullConstant.v() ), ret0 ));

      units.addLast( Jimple.v().newReturnStmt( IntConstant.v(1) ) );

      units.addLast( ret0);
      Tagger.tagChain(units, InstructionKindTag.ASPECT_CODE);
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
                                Scene.v().makeConstructorRef(cl,new ArrayList()))));
      StaticFieldRef ref = Jimple.v().
          newStaticFieldRef(Scene.v().makeFieldRef(cl,"abc$perSingletonInstance",cl.getType(),true));
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
      if(!cl.declaresMethod(SootMethod.staticInitializerName,new ArrayList()))
        throw new InternalCompilerError("should always be a static initializer here");

      debug("getting clinit");
      clinit = cl.getMethod(SootMethod.staticInitializerName,new ArrayList());
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
                        Jimple.v().newStaticInvokeExpr( postClinit.makeRef() ) );
             units.insertBefore(invokepostClinit,s);
             Tagger.tagStmt(invokepostClinit, InstructionKindTag.ASPECT_CLINIT);
             // goto return;
             Stmt goto_s = Jimple.v().newGotoStmt(nop);
             Tagger.tagStmt(goto_s, InstructionKindTag.ASPECT_CLINIT);
             units.insertBefore(goto_s,s);
             // catchlocal := @caughtexception
             Local catchLocal =
                localgen.generateLocal(RefType.v("java.lang.Throwable"),
                        "catchLocal");
             CaughtExceptionRef exceptRef =
                   Jimple.v().newCaughtExceptionRef();
             Stmt exceptionidentity =
                 Jimple.v().newIdentityStmt(catchLocal, exceptRef);
             Tagger.tagStmt(exceptionidentity, InstructionKindTag.ASPECT_CLINIT);
             units.insertBefore(exceptionidentity,s);
             // abc$initFailureCause := catchlocal
             StaticFieldRef cause =
                 Jimple.v().
                 newStaticFieldRef
                 (Scene.v().makeFieldRef
                  (cl,"abc$initFailureCause",RefType.v("java.lang.Throwable"),true));

             Stmt assigntofield =
                  Jimple.v().
                     newAssignStmt(cause,catchLocal);
             Tagger.tagStmt(assigntofield, InstructionKindTag.ASPECT_CLINIT);
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
    public void fillInPerObjectAspect( Aspect aspct )
      { SootClass cl = aspct.getInstanceClass().getSootClass();
        String aname = aspct.getName();
        String perkind =
          aspct.getPer() instanceof PerThis ? "This" : "Target";

        debug("--- BEGIN filling in per aspect "+ aname +
                 " with kind " + perkind);

        String perInterfaceName = aname + "$abc$Per" + perkind;
        // create name of field/get/set with _ instead of .
        String getSetFieldName = aname.replace('.','_') + "$abc$Per" + perkind;

        String perGetName = getSetFieldName + "Get";
        String perSetName = getSetFieldName + "Set";
        String perFieldName = getSetFieldName + "Field";

        debug("perInterfaceName is " + perInterfaceName);
        debug("perGetName is" + perGetName);
        debug("perSetName is" + perSetName);
        debug("perFieldName is" + perFieldName);

        // generate the interface
        SootClass inter =
          genPerObjectInterface(cl, perInterfaceName, perGetName, perSetName);

        // add implementation of interface to all weavable classes
        genPerObjectInterfaceImpl(cl, inter, perGetName, perSetName, perFieldName);

        // front end has put aspectOf method, fill in body
        debug(" ... adding aspectOf body");
        genPerObjectAspectOfBody(cl, inter, perGetName);

        // front end has put hasAspect method, fill in body
        debug(" ... adding hasAspect body");
        genPerObjectHasAspectBody(cl, inter, perGetName);

        // generate the per Object bind method
        debug("... adding per Object bind method");
        genPerObjectBindMethod(cl, inter, perGetName, perSetName, perkind);

        debug( "--- END filling in per aspect "+ aname +
                        " with kind " + perkind +"\n" );
      }

    public void fillInPerCflowAspect(Aspect aspct) {
        SootClass cl = aspct.getInstanceClass().getSootClass();

        genPerCflowStackField(cl);
        genPerCflowAspectOfBody(cl);
        genPerCflowHasAspectBody(cl);
        genPerCflowPushMethod(cl);
    }

    /** make interface for per instance */
    private static SootClass genPerObjectInterface( SootClass aclass,
                      String interfaceName, String getName, String setName)
      { // make the new interface
        SootClass inter = new SootClass(interfaceName,
                              Modifier.INTERFACE | Modifier.PUBLIC);
        debug("adding " + interfaceName);
        debug("name from SootClass is " + inter.getName());
        inter.setSuperclass(Scene.v().getSootClass("java.lang.Object"));

        // add the getter
        SootMethod getter = new SootMethod ( getName,
                                 new LinkedList(),
                                 aclass.getType(),
                                 Modifier.ABSTRACT | Modifier.PUBLIC);
        inter.addMethod(getter);

        // add the setter
        ArrayList param = new ArrayList();
        param.add(aclass.getType());
        SootMethod setter = new SootMethod ( setName,
                                 param,
                                 VoidType.v(),
                                 Modifier.ABSTRACT | Modifier.PUBLIC);
        inter.addMethod(setter);

        // add to the new interface to the scene as application class
        Scene.v().addClass(inter);
        inter.setApplicationClass();
        return(inter);
      }

    /** add implements interface and accessor methods to all weavable classes */
    private void genPerObjectInterfaceImpl(SootClass aclass, SootClass inter,
                                        String getName, String setName,
                                        String fieldName)
      { // TODO: right now doing ALL weavable classes as ajc does, should
        // be able to improve upon that with some analysis.  LJH
        for( Iterator clIt =
                 abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator();
                 clIt.hasNext(); )
          { final AbcClass cl = (AbcClass) clIt.next();
            final SootClass scl = cl.getSootClass();
            debug("Adding " + inter.getName() + " to " + scl.getName());

            // I don't think we want to add it to interfaces - ODM
            if (scl.isInterface()) continue;

            // add the implements
            scl.addInterface(inter);

            // add the new field
            SootField perfield = new SootField(fieldName,
                                  aclass.getType(),
                                  Modifier.PRIVATE | Modifier.TRANSIENT);
            scl.addField(perfield);


            // add the getter --------------------------------
            SootMethod getter = new SootMethod ( getName,
                                 new LinkedList(),
                                 aclass.getType(),
                                 Modifier.PUBLIC);
            Body getbody = Jimple.v().newBody(getter);
            getter.setActiveBody(getbody);
            Chain getunits = getbody.getUnits().getNonPatchingChain();
            LocalGeneratorEx lgetter = new LocalGeneratorEx(getbody);

            // gen needed locals
            Local getthis = lgetter.generateLocal(scl.getType(),"this");
            Local gfieldloc = lgetter.generateLocal(aclass.getType(),
                                                               "fieldloc");
            // create the statements
            Stmt idstmt = Jimple.v().newIdentityStmt(getthis,
                    Jimple.v().newThisRef(RefType.v(scl)));
            FieldRef fieldref = Jimple.v().
                                  newInstanceFieldRef(getthis,perfield.makeRef());
            Stmt fieldassign = Jimple.v().newAssignStmt(gfieldloc,fieldref);
            Stmt returnstmt = Jimple.v().newReturnStmt(gfieldloc);

            // insert statements into chain
            getunits.addLast(idstmt);
            getunits.addLast(fieldassign);
            getunits.addLast(returnstmt);
            Tagger.tagChain(getunits, InstructionKindTag.PEROBJECT_GET);

            // add the method
            scl.addMethod(getter);

            // add the setter -------------------------------------
            ArrayList param = new ArrayList();
            param.add(aclass.getType());
            SootMethod setter = new SootMethod ( setName,
                                 param,
                                 VoidType.v(),
                                 Modifier.PUBLIC);

            Body setbody = Jimple.v().newBody(setter);
            setter.setActiveBody(setbody);
            Chain setunits = setbody.getUnits().getNonPatchingChain();
            LocalGeneratorEx lsetter = new LocalGeneratorEx(setbody);

            // gen needed locals
            Local setthis = lsetter.generateLocal(scl.getType(),"this");
            Local sfieldloc = lsetter.generateLocal(
                                              aclass.getType(),"fieldloc");

            // create the statements
            Stmt idstmt1 = Jimple.v().newIdentityStmt(setthis,
                               Jimple.v().newThisRef(RefType.v(scl)));
            Stmt idstmt2 = Jimple.v().newIdentityStmt(sfieldloc,
                               Jimple.v().
                                     newParameterRef(aclass.getType(), 0));
            fieldref = Jimple.v().  newInstanceFieldRef(setthis,perfield.makeRef());
            fieldassign = Jimple.v().newAssignStmt(fieldref,sfieldloc);
            returnstmt = Jimple.v().newReturnVoidStmt();

            // insert the statements into chain
            setunits.addLast(idstmt1);
            setunits.addLast(idstmt2);
            setunits.addLast(fieldassign);
            setunits.addLast(returnstmt);
            Tagger.tagChain(setunits, InstructionKindTag.PEROBJECT_SET);

            // add the method
            scl.addMethod(setter);
          }
      }

    /** fill in body of <AspectName> aspectOf (java.lang.Object) */
    private void genPerObjectAspectOfBody(SootClass acl,
                      SootClass inter,
                      String getName)
      {

        // get the class for java.lang.Object
        SootClass jlo = Scene.v().getSootClass("java.lang.Object");
        List aspectOfTypes=new ArrayList(1);
        aspectOfTypes.add(jlo.getType());
        // get the aspectOf method
        SootMethod aspectOf = acl.getMethod("aspectOf",aspectOfTypes);
        // get the class for  org.aspectj.lang.noAspectBoundException
        SootClass nabe = Scene.v().getSootClass(
                              "org.aspectj.lang.NoAspectBoundException");
        SootMethodRef nabeinit = Scene.v().makeConstructorRef(nabe,new ArrayList());

        // get the method for reading the per object
        SootMethodRef getter
            = Scene.v().makeMethodRef(inter,getName,new ArrayList(),acl.getType(),false);

        // make a new body for it, and make it the active one
        Body b = Jimple.v().newBody(aspectOf);
        aspectOf.setActiveBody(b);
        Chain units = b.getUnits();

        // generate the locals needed
        LocalGeneratorEx lg = new LocalGeneratorEx(b);
        Local theObject = lg.generateLocal(jlo.getType(), "theObject");
        Local nabException = lg.generateLocal(nabe.getType(), "nabException");
        Local castedArg = lg.generateLocal(inter.getType(), "castedArg");
        Local canHavePer = lg.generateLocal(BooleanType.v(), "canHavePer");
        Local perInstance = lg.generateLocal(acl.getType(), "perInstance");

        // generate the statements
        Stmt idstmt = Jimple.v().newIdentityStmt(theObject,
                         Jimple.v().newParameterRef(jlo.getType(),0));
        Stmt assign1 = Jimple.v().newAssignStmt(canHavePer,
                          Jimple.v().
                             newInstanceOfExpr(theObject,inter.getType()));
        Stmt label0 = Jimple.v().newNopStmt(); // placeholder
        Stmt ifinstance = Jimple.v().
            newIfStmt(Jimple.v().
               newEqExpr(canHavePer,IntConstant.v(0)), label0);
        Stmt caststmt = Jimple.v().newAssignStmt(
                castedArg,Jimple.v().newCastExpr(theObject,inter.getType()));
        Stmt interfaceinvoke =
                Jimple.v().newAssignStmt (perInstance,
                    Jimple.v().newInterfaceInvokeExpr(
                      castedArg,getter));
        Stmt ifnull = Jimple.v().
            newIfStmt(Jimple.v().
              newEqExpr(perInstance,NullConstant.v()), label0);
        Stmt returnstmt = Jimple.v().newReturnStmt(perInstance);
        Stmt newexception = Jimple.v().newAssignStmt(
              nabException, Jimple.v().newNewExpr(nabe.getType()));
        Stmt initexception = Jimple.v().newInvokeStmt(
            Jimple.v().newSpecialInvokeExpr(nabException,nabeinit));
        Stmt throwstmt = Jimple.v().newThrowStmt(nabException);

        // insert the statements into the body
        units.addLast(idstmt);
        units.addLast(assign1);
        units.addLast(ifinstance);
        units.addLast(caststmt);
        units.addLast(interfaceinvoke);
        units.addLast(ifnull);
        units.addLast(returnstmt);
        units.addLast(label0);
        units.addLast(newexception);
        units.addLast(initexception);
        units.addLast(throwstmt);
        Tagger.tagChain(units, InstructionKindTag.ASPECT_CODE);
      }

    /** fill in body of boolean hasAspect(java.lang.Object) */
    private void genPerObjectHasAspectBody(SootClass acl,
                       SootClass inter, String getName)
      {
        SootClass jlo = Scene.v().getSootClass("java.lang.Object");
        List hasAspectTypes=new ArrayList(1);
        hasAspectTypes.add(jlo.getType());

        // get the hasAspect method
        SootMethod hasAspect = acl.getMethod("hasAspect",hasAspectTypes);
        // get the get and set methods for instance
        SootMethodRef getter
            = Scene.v().makeMethodRef(inter,getName,new ArrayList(),acl.getType(),false);

        // make a new body, and make it active
        Body b = Jimple.v().newBody(hasAspect);
        hasAspect.setActiveBody(b);
        Chain units = b.getUnits();

        // gen the locals needed
        LocalGeneratorEx lg = new LocalGeneratorEx(b);
        Local theObject = lg.generateLocal(jlo.getType(),"theObject");
        Local canHavePer = lg.generateLocal(BooleanType.v(), "canHavePer");
        Local castedArg = lg.generateLocal(inter.getType(), "castedArg");
        Local perInstance = lg.generateLocal(acl.getType(), "perInstance");

        // gen the statements needed
        Stmt idstmt = Jimple.v().newIdentityStmt(theObject,
                         Jimple.v().newParameterRef(jlo.getType(),0));
        Stmt assign1 = Jimple.v().newAssignStmt(canHavePer,
                          Jimple.v().
                             newInstanceOfExpr(theObject,inter.getType()));
        Stmt label0 = Jimple.v().newNopStmt(); // placeholder
        Stmt ifinstance = Jimple.v().
            newIfStmt(Jimple.v().
               newEqExpr(canHavePer,IntConstant.v(0)), label0);
        Stmt caststmt = Jimple.v().newAssignStmt(
                castedArg,Jimple.v().newCastExpr(theObject,inter.getType()));
        Stmt interfaceinvoke =
                Jimple.v().newAssignStmt (perInstance,
                    Jimple.v().newInterfaceInvokeExpr(
                      castedArg,getter));
        Stmt ifnull = Jimple.v().
            newIfStmt(Jimple.v().
              newEqExpr(perInstance,NullConstant.v()), label0);
        Stmt returnstmt_true = Jimple.v().newReturnStmt(IntConstant.v(1));
        Stmt returnstmt_false = Jimple.v().newReturnStmt(IntConstant.v(0));

        // insert the statements into the body

        units.addLast(idstmt);
        units.addLast(assign1);
        units.addLast(ifinstance);
        units.addLast(caststmt);
        units.addLast(interfaceinvoke);
        units.addLast(ifnull);
        units.addLast(returnstmt_true);
        units.addLast(label0);
        units.addLast(returnstmt_false);
        Tagger.tagChain(units, InstructionKindTag.ASPECT_CODE);
      }

    /** create method public static void abc$perThisBind(java.lang.Object)  or
                      public static void abc$perTargetBind(java.lang.Object) */
    private void genPerObjectBindMethod(SootClass acl, SootClass inter,
                   String getName, String setName, String perKind)
      { String methodname = "abc$per" + perKind + "Bind";
        // get the class for java.lang.Object, and set/get methods
        SootClass jlo = Scene.v().getSootClass("java.lang.Object");
        SootMethodRef getter = Scene.v().makeMethodRef(inter,getName,new ArrayList(),acl.getType(),false);
        ArrayList setParams=new ArrayList(1);
        setParams.add(acl.getType());
        SootMethodRef setter = Scene.v().makeMethodRef(inter,setName,setParams,VoidType.v(),false);
        SootMethodRef aclinit = Scene.v().makeConstructorRef(acl,new ArrayList());

        // make the method and add it to the class
        ArrayList param = new ArrayList();
        param.add(jlo.getType());
        SootMethod binder = new SootMethod ( methodname,
                                  param,
                                  VoidType.v(),
                                  Modifier.PUBLIC | Modifier.STATIC );
        acl.addMethod(binder);

        // get body and chain
        Body b = Jimple.v().newBody(binder);
        binder.setActiveBody(b);
        Chain units = b.getUnits().getNonPatchingChain();

        // generate the locals needed
        LocalGeneratorEx lg = new LocalGeneratorEx(b);
        Local theObject = lg.generateLocal(RefType.v("java.lang.Object"), "theObject");
        Local castedArg = lg.generateLocal(inter.getType(), "castedArg");
        Local canHavePer = lg.generateLocal(BooleanType.v(), "canHavePer");
        Local perInstance = lg.generateLocal(acl.getType(), "perInstance");
        Local newInstance = lg.generateLocal(acl.getType(), "newInstance");

        // generate the statements
        Stmt idstmt = Jimple.v().newIdentityStmt(theObject,
                         Jimple.v().newParameterRef(jlo.getType(),0));
        Stmt assign1 = Jimple.v().newAssignStmt(canHavePer,
                          Jimple.v().
                             newInstanceOfExpr(theObject,inter.getType()));
        Stmt label0 = Jimple.v().newNopStmt(); // placeholder
        Stmt ifinstance = Jimple.v().
            newIfStmt(Jimple.v().
               newEqExpr(canHavePer,IntConstant.v(0)), label0);
        Stmt caststmt = Jimple.v().newAssignStmt(
                castedArg,Jimple.v().newCastExpr(theObject,inter.getType()));
        Stmt interfaceinvokeget =
                Jimple.v().newAssignStmt (perInstance,
                    Jimple.v().newInterfaceInvokeExpr(
                      castedArg,getter));
        Stmt ifnotnull = Jimple.v().
            newIfStmt(Jimple.v().
              newNeExpr(perInstance,NullConstant.v()), label0);
        Stmt newstmt = Jimple.v().newAssignStmt(
              newInstance, Jimple.v().newNewExpr(acl.getType()));
        Stmt initstmt = Jimple.v().newInvokeStmt(
            Jimple.v().newSpecialInvokeExpr(newInstance,aclinit));
        Stmt interfaceinvokeset =
            Jimple.v().newInvokeStmt(
              Jimple.v().newInterfaceInvokeExpr(castedArg,setter,newInstance));

        Stmt returnstmt = Jimple.v().newReturnVoidStmt();

        // insert the statements into the body
        units.addLast(idstmt);
        units.addLast(assign1);
        units.addLast(ifinstance);
        units.addLast(caststmt);
        units.addLast(interfaceinvokeget);
        units.addLast(ifnotnull);
        units.addLast(newstmt);
        units.addLast(initstmt);
        units.addLast(interfaceinvokeset);
        units.addLast(label0);
        units.addLast(returnstmt);
      }

    public void genPerCflowStackField(SootClass aspct) {
        SootField perCflowStack;

        SootClass stackClass=Scene.v()
            .getSootClass("org.aspectbench.runtime.internal.CFlowStack");
        RefType stackType=stackClass.getType();

        perCflowStack=new SootField("abc$perCflowStack",stackType,
                                    Modifier.PUBLIC | Modifier.STATIC);
        debug("adding abc$perCflowStack field");
        aspct.addField(perCflowStack);

        SootMethod preClinit=new AspectCodeGen().getPreClinit(aspct);
        LocalGeneratorEx lg=new LocalGeneratorEx
            (preClinit.getActiveBody());

        Local loc=lg.generateLocal(stackType,"perCflowStack");
        Chain units=preClinit.getActiveBody().getUnits();

        Stmt returnStmt=(Stmt) units.getFirst();
        while(!(returnStmt instanceof ReturnVoidStmt))
            returnStmt=(Stmt) units.getSuccOf(returnStmt);

        units.insertBefore(Jimple.v().newAssignStmt
                           (loc,Jimple.v().newNewExpr(stackType)),
                           returnStmt);
        units.insertBefore(Jimple.v().newInvokeStmt
                           (Jimple.v().newSpecialInvokeExpr
                            (loc,Scene.v().makeConstructorRef(stackClass,new ArrayList()))),
                            returnStmt);
        units.insertBefore(Jimple.v().newAssignStmt
                           (Jimple.v().newStaticFieldRef(perCflowStack.makeRef()),loc),
                           returnStmt);
    }

    private void genPerCflowHasAspectBody(SootClass cl){
        SootMethod hasAspect;
        if (Modifier.isAbstract(cl.getModifiers()))
            return;
        // method was generated by the front end, need to give it a body
        hasAspect = cl.getMethod("hasAspect",new ArrayList());
        Body b = Jimple.v().newBody(hasAspect);
        hasAspect.setActiveBody(b);

        LocalGeneratorEx lg = new LocalGeneratorEx(b);

        Chain units = b.getUnits();

        SootClass stackClass=Scene.v()
            .getSootClass("org.aspectbench.runtime.internal.CFlowStack");
        RefType stackType=stackClass.getType();

        SootFieldRef perCflowStack=Scene.v().makeFieldRef(cl,"abc$perCflowStack",stackType,true);
        Local stackLoc=lg.generateLocal(stackType,"perCflowStack");

        units.addLast(Jimple.v().newAssignStmt
                      (stackLoc,Jimple.v().newStaticFieldRef(perCflowStack)));


        Local hasAspectLoc=lg.generateLocal(BooleanType.v(),"hasAspect");
        units.addLast(Jimple.v().newAssignStmt
                      (hasAspectLoc,
                       Jimple.v().newVirtualInvokeExpr
                       (stackLoc,
                        Scene.v().makeMethodRef
                        (stackClass,"isValid",new ArrayList(),BooleanType.v(),false))));

        units.addLast(Jimple.v().newReturnStmt(hasAspectLoc));
        Tagger.tagChain(units, InstructionKindTag.ASPECT_CODE);
    }

    private void genPerCflowAspectOfBody(SootClass cl){
        SootMethod hasAspect;
        if (Modifier.isAbstract(cl.getModifiers()))
            return;
        // method was generated by the front end, need to give it a body
        hasAspect = cl.getMethod("aspectOf",new ArrayList());
        Body b = Jimple.v().newBody(hasAspect);
        hasAspect.setActiveBody(b);

        LocalGeneratorEx lg = new LocalGeneratorEx(b);

        Chain units = b.getUnits();

        SootClass stackClass=Scene.v()
            .getSootClass("org.aspectbench.runtime.internal.CFlowStack");
        RefType stackType=stackClass.getType();

        SootFieldRef perCflowStack=Scene.v().makeFieldRef(cl,"abc$perCflowStack",stackType,true);
        Local stackLoc=lg.generateLocal(stackType,"perCflowStack");

        units.addLast(Jimple.v().
                      newAssignStmt(stackLoc,
                                    Jimple.v().newStaticFieldRef(perCflowStack)));

        Type object=Scene.v().getSootClass("java.lang.Object").getType();

        Local theAspectO=lg.generateLocal(object,"theAspectO");
        units.addLast(Jimple.v().newAssignStmt
                      (theAspectO,
                       Jimple.v().newVirtualInvokeExpr
                       (stackLoc,
                        Scene.v().makeMethodRef
                        (stackClass,"peekInstance",new ArrayList(),object,false))));

        Local theAspect=lg.generateLocal(cl.getType(),"theAspect");
        units.addLast(Jimple.v().newAssignStmt
                      (theAspect,Jimple.v().newCastExpr(theAspectO,cl.getType())));

        units.addLast(Jimple.v().newReturnStmt(theAspect));
        Tagger.tagChain(units, InstructionKindTag.ASPECT_CODE);
    }


    /** create method public static void abc$perCflowPush() */
    private void genPerCflowPushMethod(SootClass aspct) {
        SootClass stackClass=Scene.v()
            .getSootClass("org.aspectbench.runtime.internal.CFlowStack");
        RefType stackType=stackClass.getType();

        Type object=Scene.v().getSootClass("java.lang.Object").getType();

        SootMethod perCflowPush=new SootMethod
            ("abc$perCflowPush",new ArrayList(),VoidType.v(),
             Modifier.PUBLIC | Modifier.STATIC);

        Body b=Jimple.v().newBody(perCflowPush);
        perCflowPush.setActiveBody(b);

        LocalGeneratorEx lg=new LocalGeneratorEx(b);

        Chain units=b.getUnits();

        SootFieldRef perCflowStack
            =Scene.v().makeFieldRef(aspct,"abc$perCflowStack",stackType,true);
        Local stackLoc=lg.generateLocal(stackType,"perCflowStack");

        units.addLast(Jimple.v().newAssignStmt
                      (stackLoc,Jimple.v().newStaticFieldRef(perCflowStack)));

        Local aspectLoc=lg.generateLocal(aspct.getType(),"aspect");
        units.addLast(Jimple.v().newAssignStmt
                      (aspectLoc,Jimple.v().newNewExpr(aspct.getType())));
        units.addLast(Jimple.v().newInvokeStmt
                      (Jimple.v().newSpecialInvokeExpr
                       (aspectLoc,Scene.v().makeConstructorRef(aspct,new ArrayList()))));

        ArrayList pushArgs=new ArrayList(1);
        pushArgs.add(object);

        units.addLast(Jimple.v().newInvokeStmt
                      (Jimple.v().newVirtualInvokeExpr
                       (stackLoc,
                        Scene.v().makeMethodRef
                        (stackClass,"pushInstance",pushArgs,VoidType.v(),false),
                        aspectLoc)));

        units.addLast(Jimple.v().newReturnVoidStmt());


        aspct.addMethod(perCflowPush);

    }


    public SootMethod getPreClinit(SootClass cl) {
        // FIXME: ShadowPointsSetter.restructureBody has special knowledge of this method,
        // so that it can put the nop after the call if it has already been introduced.
        // I apologise for introducing yet more weaving intricacies, but probably this whole
        // thing should be redesigned. Please yell at me to fix it properly if it causes
        // you any trouble -- Ganesh

        if(cl.declaresMethod("abc$preClinit",new ArrayList(),VoidType.v()))
            return cl.getMethod("abc$preClinit",new ArrayList(),VoidType.v());

        SootMethod preClinit=new SootMethod
            ("abc$preClinit",new ArrayList(),
             VoidType.v(),Modifier.PRIVATE | Modifier.STATIC);

        SootMethod clinit=cl.getMethod(SootMethod.staticInitializerName,new ArrayList());

        Body b = Jimple.v().newBody(preClinit);
        preClinit.setActiveBody(b);

        b.getUnits().addLast(Jimple.v().newReturnVoidStmt());

        cl.addMethod(preClinit);

        // There shouldn't be any identity statements;
        // perhaps we should do getFirstRealStmt or whatever just in case,
        // though
        InvokeStmt preClinitInvoke = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(preClinit.makeRef()));
        Tagger.tagStmt(preClinitInvoke, InstructionKindTag.ASPECT_CLINIT);
        clinit.getActiveBody().getUnits().addFirst(preClinitInvoke);

        return preClinit;

    }


    /* =============== PERCFLOW/PERCFLOWBELOW ASPECT ============= */
    // aspectOf

    // hasAspect

    // add static for cflow stack, add initializer in preClinit()

}

package abc.weaving.weaver;

import soot.*;
import soot.tagkit.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.LocalGenerator;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;

/** The purpose of this class is to iterate over all AdviceApplication
 *    instances for a Class and to insert the relevant code for the 
 *    Static Join Points. 
 *
 * @author Laurie Hendren
 * @date May 11, 2004
 */

public class GenStaticJoinPoints {

    /** only want to generate the factory for the first SJP of a class.
        It should be reset to false at the beginning of each class.  */
    private  boolean factory_generated = false;

    /** only want to load it once */
    private boolean factory_loaded = false;

    /** local variable pointing to factory */
    private Local factory_local;

    /** count of number of SJPs created  */
    private int num_SJP_created = 0;

    /** return current count, and then incr counter */
    public int incrNumSJP () { return(num_SJP_created++); }

    private static void debug(String message)
      { if (abc.main.Debug.v().genStaticJoinPoints) 
	   System.err.println("GSJP*** " + message);
      }	

    /** generate code for all the static join points in class sc */
    public void genStaticJoinPoints(SootClass sc) {
      debug("--- BEGIN Generating Static Join Points for class " + 
	  sc.getName());

      // no factory for this class yet
      factory_generated = false;

      // reset the counter to 0
      num_SJP_created = 0;

      // the source file
      SootMethod clinit;  // where we have to insert the initalizer
      Body b;             // its body
      Chain units;        // its units 
      LocalGenerator lg;  // its localgen
      Stmt ip;            // the instruction from with to insert (before)

      // --- get the units and insertion point in clinit()
      if (sc.declaresMethod("void <clinit>()"))
        { debug("Found the clinit in which to put the SJP");
          clinit = sc.getMethod("void <clinit>()");
          b = clinit.retrieveActiveBody();
          units = b.getUnits();
          lg = new LocalGenerator(b);
	  ip = (Stmt) units.getFirst();  // should be the return stmt 
        }
      else
        throw new CodeGenException(
           "SJP insertion assumes a clinit existed " +
	   "in class " + sc.getName());

      // for each method in the class 
      for( Iterator methodIt = sc.getMethods().iterator(); 
	   methodIt.hasNext(); ) {

	 // get the next method
         final SootMethod method = (SootMethod) methodIt.next();

	 // nothing to do for abstract or native methods 
         if( method.isAbstract() ) continue;
         if( method.isNative() ) continue;

	 // get all the advice list for this method
         MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);

	 // if no advice list for this method, nothing to do
	 if ((adviceList == null) || adviceList.isEmpty())
           { debug("No advice list for method " + method.getName());
	     continue;
	   }
         
         debug("   --- BEGIN generating static join points for method " + 
	                method.getName());

	 // --- Deal with each of the four lists 
	 if (adviceList.hasBodyAdvice())
	    genSJPformethod(sc,units,ip,lg,
		         method,adviceList.bodyAdvice);

         if (adviceList.hasInitializationAdvice())
	    genSJPformethod(sc,units,ip,lg,
		         method,adviceList.initializationAdvice);

	 if (adviceList.hasPreinitializationAdvice())
	    genSJPformethod(sc,units,ip,lg,
		         method,adviceList.preinitializationAdvice);

	 if (adviceList.hasStmtAdvice())
	    genSJPforstmtlist(sc,units,ip,lg,
		         method,adviceList.stmtAdvice);

	 debug("   --- END Generating Static Join Points for method " + 
	                    method.getName() + "\n");
       } // for each method

      debug(" --- END Generating Static Join Points for class " + 
	                sc.getName() + "\n");
    } // setStaticJoinPoints


  //TODO: need to think about thisenclosingjoinpoint stuff
  private void genSJPformethod(SootClass sc, 
                        Chain units, Stmt ip, LocalGenerator lg,
                        SootMethod method, 
	                List /*<AdviceApplication>*/ adviceApplList) {
     
     SootField thisSJPfield = null; // set this when we make the first one

     for (Iterator alistIt = adviceApplList.iterator(); alistIt.hasNext();)
        { final AdviceApplication adviceappl = 
	                  (AdviceApplication) alistIt.next(); 
	  // find out if the advice method needs that static join point
	  AdviceDecl advicedecl = adviceappl.advice;
	  if ( advicedecl == null // a dummy advice just for SJP
	       || advicedecl.hasJoinPointStaticPart() 
	       || advicedecl.hasJoinPoint()) // need to create a SJP
	    { debug("Need a SJP ");
	      if (!factory_generated) // must generate the code for factory
	        { debug(" --- Generating code for the factory");
                  genSJPFactory(sc, units, ip, lg);
		  factory_generated = true; // a field to remember we have one 
		}

	      // increment counter for number of times this advice method
	      // applies .... don't know what we will use that for yet
	      if (advicedecl != null) 
	         advicedecl.incrApplCount();

              if (thisSJPfield == null) // haven't made one yet
	         thisSJPfield =  
		     makeSJPfield(sc,units,ip,lg,method,adviceappl);
              debug("setting " + adviceappl + " to " + thisSJPfield);
   	      adviceappl.sjpInfo.sjpfield = thisSJPfield; // store in adviceappl
	    } // if we need a SJP
	} // each advice for the SJP
    } // genSJPformethodmethod 


  private void genSJPforstmtlist(SootClass sc, 
                        Chain units, Stmt ip, LocalGenerator lg,
                        SootMethod method, 
	                List /*<AdviceApplication>*/ adviceApplList) {
     
     // keep track of mapping from stmt -> SJP
     IdentityHashMap SJPhashtable = new IdentityHashMap();

     for (Iterator alistIt = adviceApplList.iterator(); alistIt.hasNext();)
        { final AdviceApplication adviceappl = 
	                  (AdviceApplication) alistIt.next(); 
	  // find out if the advice method needs that static join point
	  AdviceDecl advicedecl = adviceappl.advice;
	  if (advicedecl == null  || // FIXME: is this really needed
	      advicedecl.hasJoinPointStaticPart() ||
	      advicedecl.hasJoinPoint()) // need to create a SJP
	    { debug("Need a SJP ");
	      if (!factory_generated) // must generate the code for factory
	        { debug(" --- Generating code for the factory");
                  genSJPFactory(sc, units, ip, lg);
		  factory_generated = true; // a field to remember we have one 
		}

	      // increment counter for number of times this advice method
	      // applies .... don't know what we will use that for yet
	      if (advicedecl != null) advicedecl.incrApplCount();

	      // get the stmt for the mapping
              Stmt keystmt = null;
	      if (adviceappl instanceof HandlerAdviceApplication)
                 keystmt = ((HandlerAdviceApplication) adviceappl).stmt; 
	      else if (adviceappl instanceof NewStmtAdviceApplication)
                 keystmt = ((NewStmtAdviceApplication) adviceappl).stmt;
              else if (adviceappl instanceof StmtAdviceApplication)
                 keystmt = ((StmtAdviceApplication) adviceappl).stmt; 
	      else
	        throw new CodeGenException(
	                  "Unknown kind of advice for inside method body: " + 
		           adviceappl);
	        
	      // lookup the key stmt to see if it has a SJP yet
	      SootField thisSJPfield;
              if (SJPhashtable.containsKey(keystmt)) // already have one
                 thisSJPfield = (SootField) SJPhashtable.get(keystmt);
              else	
	      { thisSJPfield =  
		     makeSJPfield(sc,units,ip,lg,method,adviceappl);
		// put it in the table
		SJPhashtable.put(keystmt,thisSJPfield);
               }	
   	      adviceappl.sjpInfo.sjpfield = thisSJPfield; // store in adviceappl
	    } // if we need a SJP
	} // each advice for the SJP
    } // genSJPformethodmethod 

    private void genSJPFactory(SootClass sc, Chain units, 
	                          Stmt ip, LocalGenerator lg)
      { // javaclass = java.lang.Class.forName(<myclassname>)
        Local javaclass = lg.generateLocal(
        RefType.v("java.lang.Class"));
        Value arg = StringConstant.v(sc.getName());
        SootClass jls = Scene.v().getSootClass("java.lang.Class");
        SootMethod forname = 
              jls.getMethod("java.lang.Class forName(java.lang.String)");
        Value val = Jimple.v().newStaticInvokeExpr(forname,arg);
        Stmt getjavaclass = Jimple.v().newAssignStmt(javaclass,val);
        debug("Generating getjavaclass " + getjavaclass);
        units.insertBefore(getjavaclass,ip);

        // make sure the Factory class is loaded in Soot
        if (!factory_loaded)
          {  Scene.v().loadClassAndSupport(
	             "org.aspectj.runtime.reflect.Factory");
             factory_loaded = true;
           }

        // factory_local = new Factory;
        factory_local =  
        lg.generateLocal(RefType.v("org.aspectj.runtime.reflect.Factory"));
        Stmt newfactory = Jimple.v().
        newAssignStmt(factory_local, 
			Jimple.v().newNewExpr(
			  RefType.v("org.aspectj.runtime.reflect.Factory")));
        debug("Generating newfactory " + newfactory);
        units.insertBefore(newfactory,ip);
                  
        // factory_local.<init>("sourcefile",javaclass);
        SootClass fc = Scene.v().
	     getSootClass("org.aspectj.runtime.reflect.Factory");
        SootMethod finit = 
	     fc.getMethod(
			 "void <init>(java.lang.String,java.lang.Class)");
        ArrayList args = new ArrayList(2);
	debug("tags attached to class are : " + sc.getTags());
	SourceFileTag sft = (SourceFileTag) sc.getTag("SourceFileTag");
	debug("sourcefilename of sc : " + sft);
	// FIXME: should be sourcefile, how to get this info??
        args.add(StringConstant.v(sft.getSourceFile())); 
        args.add(javaclass);
         Stmt initfactory = Jimple.v().
		     newInvokeStmt( Jimple.v().
			 newSpecialInvokeExpr(factory_local,finit,args));
        debug("Generating init " + initfactory);
        units.insertBefore(initfactory,ip);
       }


  private SootField makeSJPfield(SootClass sc, Chain units, Stmt ip,
                         LocalGenerator lg, SootMethod method,
			 AdviceApplication adviceappl) 
    { // create the name for the SJP field 
      // the kind of SJP, but made into a valid id
      String idkind = adviceappl.sjpInfo.kind.replace('-','_');
      // the method in which the SJP is found, but made into a valid id
      String idmethod = method.getName().replace('<','I').replace('>','I');
      // the current number of SJP in this class
      int sjpcount = incrNumSJP();
      String SJPName = "SJP" + sjpcount + "$" + idkind + "$" + idmethod;
      debug("The name of the field being created is " + SJPName);	      

      // create the static field
      SootField newsjpfield = 
         new SootField( SJPName, 
			 RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
			 Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);

      // insert field into class
      sc.addField(newsjpfield);

      // save field in AdviceApplication
      adviceappl.sjpInfo.sjpfield =  newsjpfield;

      // put initialization for the field in clinit()
      StaticFieldRef newfieldref = Jimple.v().newStaticFieldRef(newsjpfield);


      String sigtypeclass = adviceappl.sjpInfo.signatureTypeClass;
      String sigtype = adviceappl.sjpInfo.signatureType;
      debug("The class of constructor is " + sigtypeclass);
      debug("The type of constructor is " + sigtype);
      debug("The kind is " + adviceappl.sjpInfo.kind);
      debug("The signature is " + adviceappl.sjpInfo.signature);
      debug("The line is " + adviceappl.sjpInfo.row + 
  	          " and the column is " + adviceappl.sjpInfo.col);

      // get the signature object
      Local sigloc = lg.generateLocal(
	  RefType.v("org.aspectj.lang.reflect."+sigtypeclass));

      SootClass fc = Scene.v().
	     getSootClass("org.aspectj.runtime.reflect.Factory");
      debug("Got the factory class: " + fc);

      SootMethod sigmethod = fc.getMethod(
	     "org.aspectj.lang.reflect." + sigtypeclass + 
	     " " + sigtype + "(java.lang.String)");
      debug("Got the sig builder method: " + sigmethod);

      Stmt makesig = Jimple.v().
	newAssignStmt(sigloc, Jimple.v().
	   newVirtualInvokeExpr(factory_local,sigmethod,
	      StringConstant.v(adviceappl.sjpInfo.signature)));
      debug("Made the sig creation call " + makesig);
      units.insertBefore(makesig,ip);

      // get the SJP object
      Local sjploc = lg.generateLocal(
	 RefType.v("org.aspectj.lang.JoinPoint$StaticPart")); 
      SootMethod makeSJP = fc.getMethod(
	  "org.aspectj.lang.JoinPoint$StaticPart " +
	  "makeSJP(java.lang.String,org.aspectj.lang.Signature,int,int)");

      ArrayList args = new ArrayList();
      args.add(StringConstant.v(adviceappl.sjpInfo.kind));
      args.add(sigloc);
      args.add(IntConstant.v(adviceappl.sjpInfo.row));
      args.add(IntConstant.v(adviceappl.sjpInfo.col));

      Stmt getSJP = Jimple.v().
	newAssignStmt(sjploc, Jimple.v().
	  newVirtualInvokeExpr(factory_local,makeSJP,args));
      debug("Made SJP creation call" + getSJP);
      units.insertBefore(getSJP,ip);

      // assign the SJP object to the field
      Stmt assignField = 
	  Jimple.v().newAssignStmt(newfieldref,sjploc);
      units.insertBefore(assignField,ip);

      // return the field
      return(newsjpfield);
    } 
} // class GenStaticJoinPoints 

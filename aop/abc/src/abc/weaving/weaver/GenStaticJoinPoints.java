package abc.weaving.weaver;

import java.util.*;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.tagkit.*;
import soot.util.*;
import soot.jimple.*;
import soot.javaToJimple.LocalGenerator;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;

/** The purpose of this class is to iterate over all SJPInfo
 *    instances for a Class and to insert the relevant code for the 
 *    Static Join Points. 
 *
 * @author Laurie Hendren
 * @author Ganesh Sittampalam
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
      if (sc.declaresMethod(SootMethod.staticInitializerName,new ArrayList())) {
	  debug("Found the clinit in which to put the SJP");
	  clinit = sc.getMethod(SootMethod.staticInitializerName,new ArrayList());
          b = clinit.retrieveActiveBody();
          units = b.getUnits();
          lg = new LocalGenerator(b);
	  ip = (Stmt) units.getFirst();  // should be the return stmt 
        }
      else
        throw new InternalCompilerError(
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
         List/*<SJPInfo>*/ sjpInfoList = 
	     GlobalAspectInfo.v().getSJPInfoList(method);

         debug("   --- BEGIN generating static join points for method " + 
	                method.getName());

	 Iterator it=sjpInfoList.iterator();
	 while(it.hasNext()) {
	     SJPInfo sjpinfo=(SJPInfo) (it.next());

	     
	      if (!factory_generated) // must generate the code for factory
	        { debug(" --- Generating code for the factory");
                  genSJPFactory(sc, units, ip, lg);
		  factory_generated = true; // a field to remember we have one 
		}
	      sjpinfo.sjpfield=makeSJPfield(sc,units,ip,lg,method,sjpinfo);
	 }
      }
    }

    private void genSJPFactory(SootClass sc, Chain units, 
	                          Stmt ip, LocalGenerator lg)
      { // javaclass = java.lang.Class.forName(<myclassname>)
        Local javaclass = lg.generateLocal(
        RefType.v("java.lang.Class"));
        Value arg = StringConstant.v(sc.getName());
        SootClass jls = Scene.v().getSootClass("java.lang.Class");
	List fornameParams=new ArrayList(1);
	fornameParams.add(RefType.v("java.lang.String"));
        SootMethodRef forname 
	    = Scene.v().makeMethodRef(jls,"forName",fornameParams,RefType.v("java.lang.Class"));
        Value val = Jimple.v().newStaticInvokeExpr(forname,arg);
        Stmt getjavaclass = Jimple.v().newAssignStmt(javaclass,val);
        debug("Generating getjavaclass " + getjavaclass);
        units.insertBefore(getjavaclass,ip);

        // make sure the Factory class is loaded in Soot
        if (!factory_loaded)
          {  Scene.v().getSootClass(
	             "abc.runtime.reflect.AbcFactory");
             factory_loaded = true;
           }

        // factory_local = new Factory;
        factory_local =  
        lg.generateLocal(RefType.v("abc.runtime.reflect.AbcFactory"));
        Stmt newfactory = Jimple.v().
        newAssignStmt(factory_local, 
			Jimple.v().newNewExpr(
			  RefType.v("abc.runtime.reflect.AbcFactory")));
        debug("Generating newfactory " + newfactory);
        units.insertBefore(newfactory,ip);
                  
        // factory_local.<init>("sourcefile",javaclass);
        SootClass fc = Scene.v().
	     getSootClass("abc.runtime.reflect.AbcFactory");
	List finitParams=new ArrayList(2);
	finitParams.add(RefType.v("java.lang.String"));
	finitParams.add(RefType.v("java.lang.Class"));
        SootMethodRef finit = Scene.v().makeConstructorRef(fc,finitParams);

        ArrayList args = new ArrayList(2);
	debug("tags attached to class are : " + sc.getTags());
	SourceFileTag sft = (SourceFileTag) sc.getTag("SourceFileTag");
	debug("sourcefilename of sc : " + sft);
        args.add(StringConstant.v(sft==null ? "<Unknown>" : sft.getSourceFile())); 
        args.add(javaclass);
         Stmt initfactory = Jimple.v().
		     newInvokeStmt( Jimple.v().
			 newSpecialInvokeExpr(factory_local,finit,args));
        debug("Generating init " + initfactory);
        units.insertBefore(initfactory,ip);
       }


  private SootField makeSJPfield(SootClass sc, Chain units, Stmt ip,
                         LocalGenerator lg, SootMethod method,
			 SJPInfo sjpInfo) 
    { // look for interfaces in the right place
	// FIXME: shouldn't know about the extension in the base code
      String classpath = sjpInfo.kind.equals("cast") ? "abc.lang.reflect." : "org.aspectj.lang.reflect.";
            
      // create the name for the SJP field 
      // the kind of SJP, but made into a valid id
      String idkind = sjpInfo.kind.replace('-','_');
      // the method in which the SJP is found, but made into a valid id
      String idmethod = method.getName().replace('<','I').replace('>','I');
      // the current number of SJP in this class
      int sjpcount = incrNumSJP();
      String SJPName = "SJP" + sjpcount + "$" + idkind + "$" + idmethod;
      debug("The name of the field being created is " + SJPName);	      

	  int  mod;
	  if (sc.isInterface())
	  	mod = Modifier.PUBLIC;
	  else
	  	mod = Modifier.PRIVATE;
	  mod = mod | Modifier.STATIC | Modifier.FINAL;
      // create the static field
      SootField newsjpfield = 
         new SootField( SJPName, 
			 RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
			 mod);

      // insert field into class
      sc.addField(newsjpfield);

      // put initialization for the field in clinit()
      StaticFieldRef newfieldref = Jimple.v().newStaticFieldRef(newsjpfield.makeRef());


      String sigtypeclass = sjpInfo.signatureTypeClass;
      String sigtype = sjpInfo.signatureType;
      debug("The class of constructor is " + sigtypeclass);
      debug("The type of constructor is " + sigtype);
      debug("The kind is " + sjpInfo.kind);
      debug("The signature is " + sjpInfo.signature);
      debug("The line is " + sjpInfo.row + 
  	          " and the column is " + sjpInfo.col);

      // get the signature object
      Local sigloc = lg.generateLocal(
	  RefType.v(classpath+sigtypeclass));

      SootClass fc = Scene.v().
	     getSootClass("abc.runtime.reflect.AbcFactory");
      debug("Got the factory class: " + fc);

      List sigmethodParams=new ArrayList(1);
      sigmethodParams.add(RefType.v("java.lang.String"));
      SootMethodRef sigmethod 
	  = Scene.v().makeMethodRef(fc,sigtype,sigmethodParams,RefType.v(classpath+sigtypeclass));
      debug("Got the sig builder method: " + sigmethod);

      Stmt makesig = Jimple.v().
	newAssignStmt(sigloc, Jimple.v().
	   newVirtualInvokeExpr(factory_local,sigmethod,
	      StringConstant.v(sjpInfo.signature)));
      debug("Made the sig creation call " + makesig);
      units.insertBefore(makesig,ip);

      // get the SJP object
      Local sjploc = lg.generateLocal(
	 RefType.v("org.aspectj.lang.JoinPoint$StaticPart")); 

      List makeSJPParams=new ArrayList(4);
      makeSJPParams.add(RefType.v("java.lang.String"));
      makeSJPParams.add(RefType.v("org.aspectj.lang.Signature"));
      makeSJPParams.add(IntType.v());
      makeSJPParams.add(IntType.v());
      SootMethodRef makeSJP 
	  = Scene.v().makeMethodRef(fc,
				    "makeSJP",
				    makeSJPParams,
				    RefType.v("org.aspectj.lang.JoinPoint$StaticPart"));


      ArrayList args = new ArrayList();
      args.add(StringConstant.v(sjpInfo.kind));
      args.add(sigloc);
      args.add(IntConstant.v(sjpInfo.row));
      args.add(IntConstant.v(sjpInfo.col));

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

package abc.weaving.weaver;

import soot.*;
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

    /** set to false to disable debugging messages for ShadowPointsSetter */
    public static boolean debug = true;

    /** only want to generate the factory for the first SJP of a class */
    private  boolean factory_generated = false;

    private static void debug(String message)
      { if (debug) System.err.println("GSJP*** " + message);
      }	


    /** generate code for all the static join points in class sc */
    public void genStaticJoinPoints(SootClass sc) {
      debug("--- BEGIN Generating Static Join Points for class " + 
	  sc.getName());

      // no factory for this class yet
      factory_generated = false;

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
	  ip = (Stmt) units.getLast();  // should be the return stmt 
        }
      else
        throw new CodeGenException(
           "SJP insertion assumes a clinit existed " +
	   "in class " + sc.getName());
      /*
        { debug("Shouldn't have to insert a clinit");
          clinit = new SootMethod( "<clinit>", 
              new ArrayList(), VoidType.v(), Modifier.STATIC );
           sc.addMethod( clinit );
           b = Jimple.v().newBody(clinit);
           clinit.setActiveBody(b);
           units = b.getUnits();
           units.addLast( Jimple.v().newReturnVoidStmt() );
           lg = new LocalGenerator(b);
           ip = (Stmt) units.getLast();  // should be the return stmt 
         }
	 */

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
	    genSJPmethod(sc,units,ip,lg,
		         method,adviceList.bodyAdvice);

	 if (adviceList.hasStmtAdvice())
	    genSJPmethod(sc,units,ip,lg,
		         method,adviceList.bodyAdvice);

         if (adviceList.hasInitializationAdvice())
	    genSJPmethod(sc,units,ip,lg,
		         method,adviceList.bodyAdvice);

	 if (adviceList.hasPreinitializationAdvice())
	    genSJPmethod(sc,units,ip,lg,
		         method,adviceList.bodyAdvice);

	 debug("   --- END Generating Static Join Points for method " + 
	                    method.getName() + "\n");
       } // for each method

      debug(" --- END Generating Static Join Points for class " + 
	                sc.getName() + "\n");
    } // setStaticJoinPoints


  //TODO: need to think about thisenclosingjoinpoint stuff
  private void genSJPmethod(SootClass sc, 
                            Chain units, Stmt ip, LocalGenerator lg,
                            SootMethod method, 
			    List /*<AdviceApplication>*/ adviceApplList) {

     for (Iterator alistIt = adviceApplList.iterator(); alistIt.hasNext();)
        { final AdviceApplication adviceappl = 
	                  (AdviceApplication) alistIt.next(); 
	  // find out if the advice method needs that static join point
	  AdviceDecl advicedecl = adviceappl.advice;
	  if (advicedecl.hasJoinPointStaticPart() ||
	      advicedecl.hasJoinPoint()) // need to create a SJP
	    { debug("Need to create a SJP ");
	      if (!factory_generated) // must generate the code for factory
	        { debug("Generating code for the factory");
		  factory_generated = true;
		}
	      // now generate code for this SJP
	      debug("The type of constructor is " + 
		              adviceappl.sjpInfo.signatureType);
	      debug("The signature is " + adviceappl.sjpInfo.signature);
	      debug("The line is " + adviceappl.sjpInfo.row + 
		     " and the column is " + adviceappl.sjpInfo.col);
             }
	} // each advice
  } // genSJPmethod 

} // class GenStaticJoinPoints 

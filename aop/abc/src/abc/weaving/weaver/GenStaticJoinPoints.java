/* abc - The AspectBench Compiler
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.tagkit.SourceFileTag;
import soot.util.Chain;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.SJPInfo;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;

/** The purpose of this class is to iterate over all SJPInfo
 *    instances for a Class and to insert the relevant code for the 
 *    Static Join Points. 
 *
 * @author Laurie Hendren
 * @author Ganesh Sittampalam
 */

public class GenStaticJoinPoints {

    private String runtimeFactoryClass = abc.main.Main.v().getAbcExtension().runtimeSJPFactoryClass();
    
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
	     abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getSJPInfoList(method);

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
	      sjpinfo.makeSJPfield(sc,units,ip,lg,method,factory_local,incrNumSJP());
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
	    = Scene.v().makeMethodRef(jls,"forName",fornameParams,RefType.v("java.lang.Class"),true);
        Value val = Jimple.v().newStaticInvokeExpr(forname,arg);
        Stmt getjavaclass = Jimple.v().newAssignStmt(javaclass,val);
        Tagger.tagStmt(getjavaclass, InstructionKindTag.THISJOINPOINT);
        debug("Generating getjavaclass " + getjavaclass);
        units.insertBefore(getjavaclass,ip);

	/* This code should be redundant with the new resolver stuff
        // make sure the Factory class is loaded in Soot
        if (!factory_loaded)
          {  Scene.v().getSootClass(runtimeFactoryClass);
             factory_loaded = true;
           }
	*/

        // factory_local = new Factory;
        factory_local =  
        lg.generateLocal(RefType.v(runtimeFactoryClass));
        Stmt newfactory = Jimple.v().
        newAssignStmt(factory_local, 
			Jimple.v().newNewExpr(
			  RefType.v(runtimeFactoryClass)));
        Tagger.tagStmt(newfactory, InstructionKindTag.THISJOINPOINT);
        debug("Generating newfactory " + newfactory);
        units.insertBefore(newfactory,ip);
                  
        // factory_local.<init>("sourcefile",javaclass);
        SootClass fc = Scene.v().
	     getSootClass(runtimeFactoryClass);
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
        Tagger.tagStmt(initfactory, InstructionKindTag.THISJOINPOINT);
        debug("Generating init " + initfactory);
        units.insertBefore(initfactory,ip);
       }

} // class GenStaticJoinPoints 

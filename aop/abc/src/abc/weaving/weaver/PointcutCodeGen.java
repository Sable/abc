package abc.weaving.weaver;
import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;
import soot.util.HashChain;
import abc.soot.util.*;
import abc.weaving.aspectinfo.AbcType;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AfterAdvice;
import abc.weaving.aspectinfo.AfterReturningAdvice;
import abc.weaving.aspectinfo.AfterReturningArgAdvice;
import abc.weaving.aspectinfo.AfterThrowingAdvice;
import abc.weaving.aspectinfo.AfterThrowingArgAdvice;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.BeforeAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.Residue;

public class PointcutCodeGen {

   private static void debug(String message)
     { if (abc.main.Debug.v().pointcutCodeGen) 
          System.err.println("PCG*** " + message);
     }

   public void weaveInAspectsPass( SootClass cl, int pass) {
     for( Iterator methodIt = cl.getMethods().iterator(); 
	     methodIt.hasNext(); ) 
       { // get the next method
         final SootMethod method = (SootMethod) methodIt.next();

	 // nothing to do for abstract or native methods 
         if( method.isAbstract() ) continue;
         if( method.isNative() ) continue;

	 // get all the advice list for this method
         MethodAdviceList adviceList = 
	     GlobalAspectInfo.v().getAdviceList(method);

	 switch (pass) {
	 case 1: // ----------------------- PASS 1 -------------
	 // if no advice list for this method, nothing to do
	 if ( (adviceList == null) || 
	      (!adviceList.hasBodyAdvice() && !adviceList.hasStmtAdvice())
	    )
           { debug("No body or stmt advice for method " + method.getName());
	     continue;
	   }

	 // have something to do ...
         debug("   --- BEGIN weaveInAspectsPass " + pass + " for method " + 
	                method.getName());
         Body b = method.getActiveBody();
         LocalGeneratorEx localgen = new LocalGeneratorEx(b);

	//	do the stmt advice
	   for (Iterator alistIt = adviceList.stmtAdvice.iterator(); 
	   alistIt.hasNext();)
		 { final AdviceApplication execappl = 
			(AdviceApplication) alistIt.next(); 
		weave_one(cl,method,localgen,execappl);
	 }  // each stmt advice
		 
	 // do the body advice 
         for (Iterator alistIt = adviceList.bodyAdvice.iterator(); 
	      alistIt.hasNext();)
           { final AdviceApplication execappl = 
	          (AdviceApplication) alistIt.next(); 
	     weave_one(cl,method,localgen,execappl);
	   } // each body advice 

	 
         break;

	 case 2: // ----------------------- PASS 2 ----------------
	 // if no advice list for this method, nothing to do
	 if ( (adviceList == null) || 
	      ( !adviceList.hasInitializationAdvice() && 
	        !adviceList.hasPreinitializationAdvice()
              )  ||
	     // FIXME: shouldn't need this check
	     (!method.getName().equals("<init>"))
	    ) 
           { debug("No init or preinit advice for method " + method.getName());
	     continue;
	   }

	 // have something to do ...
         Body b2 = method.getActiveBody();
         LocalGeneratorEx localgen2 = new LocalGeneratorEx(b2);

	 // do the init advice 
         for (Iterator alistIt = adviceList.initializationAdvice.iterator(); 
	      alistIt.hasNext();)
           { final AdviceApplication initappl = 
	          (AdviceApplication) alistIt.next(); 
	     weave_one(cl,method,localgen2,initappl);
	   } // each init advice 

	 // do the preinit advice 
         for (Iterator alistIt = adviceList.preinitializationAdvice.iterator(); 
	      alistIt.hasNext();)
           { final AdviceApplication preinitappl = 
	          (AdviceApplication) alistIt.next(); 
	     weave_one(cl,method,localgen2,preinitappl);
	   } // each preinit advice 
	   break;

	 default: // ------------------------- DEFAULT --------------
	   throw new CodeGenException("Undefined pass");
         }

         debug("   --- END weaveInAspectsPass " + pass + " for method " + 
	                method.getName() + "\n");
	} // each method 
   } // method weaveInAspectsPass1
	 
	    
    private void weave_one( SootClass cl, SootMethod method,
                            LocalGeneratorEx localgen, 
			    AdviceApplication adviceappl)
      { AdviceDecl advicedecl = adviceappl.advice;
	if ( advicedecl == null) // it was a dummy advice to enforce a SJP
	  return;
        AdviceSpec advicespec = advicedecl.getAdviceSpec();	
	if ( advicespec instanceof BeforeAdvice ) 
           BeforeWeaver.doWeave(method, localgen, adviceappl);
        else if ( advicespec instanceof AfterReturningAdvice )
           AfterReturningWeaver.doWeave(method, localgen, adviceappl);
	else if ( advicespec instanceof AfterThrowingAdvice)
           AfterThrowingWeaver.doWeave(method, localgen, adviceappl);
	else if (advicespec instanceof AfterAdvice)
	   {  
	      AfterThrowingWeaver.doWeave(method,localgen,adviceappl);
	      AfterReturningWeaver.doWeave(method,localgen,adviceappl);
	   }
	else if (advicespec instanceof AroundAdvice ) 
	   AroundWeaver.doWeave(cl, method, localgen, adviceappl);
	else 
	   throw new CodeGenException ("Unsupported kind of advice: " +
	                                 advicespec); 
      }  // method weave_one

	public static WeavingContext makeWeavingContext(AdviceApplication adviceappl) {
		int nformals = adviceappl.advice.numFormals();
		debug("There are " + nformals + " formals to the advice method.");
		Vector arglist = new Vector(nformals, 2);
		arglist.setSize(nformals);
		return new WeavingContext(arglist);
	}
	
    /** create the invoke to call the advice body */
	public static Chain makeAdviceInvokeStmt(
		Local aspectref,
		AdviceApplication adviceappl,
		Chain units,
		LocalGeneratorEx localgen,
		WeavingContext wc) {

		AdviceDecl advicedecl = adviceappl.advice;
		SootClass sootaspect =
			advicedecl.getAspect().getInstanceClass().getSootClass();
		SootMethod advicemethod = advicedecl.getImpl().getSootMethod();

		Chain c = new HashChain();

		// try to fill in the remaining formals
		//   --- first the join point ones
		if (advicedecl.hasJoinPointStaticPart()) {
			int position = advicedecl.joinPointStaticPartPos();
			debug("The index for hasJoinPointStaticPart is " + position);
			// FIXME: should really be ref to static field for SJP
			StaticFieldRef sjpfieldref =
				Jimple.v().newStaticFieldRef(adviceappl.sjpInfo.sjpfield);
			Local sjploc =
				localgen.generateLocal(
					RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
					"sjp");
			Stmt assignsjp = Jimple.v().newAssignStmt(sjploc, sjpfieldref);
			c.addLast(assignsjp);
			debug(
				"inserting at postion "
					+ position
					+ " into a Vector of size "
					+ wc.arglist.capacity());
			wc.arglist.setElementAt(sjploc, position);
		}
		if (advicedecl.hasJoinPoint()) {
			debug("The index for hasJoinPoint is " + advicedecl.joinPointPos());
		}
		if (advicedecl.hasEnclosingJoinPoint()) {
			int position = advicedecl.enclosingJoinPointPos();
			debug(
				"The index for enclosingJoinPoint is "
					+ advicedecl.enclosingJoinPointPos());
			StaticFieldRef sjpencfieldref =
				Jimple.v().newStaticFieldRef(adviceappl.sjpEnclosing.sjpfield);
			debug("The field ref is " + sjpencfieldref);
			Local sjpencloc =
				localgen.generateLocal(
					RefType.v("org.aspectj.lang.JoinPoint$StaticPart"),
					"sjpenc");
			Stmt assignsjpenc =
				Jimple.v().newAssignStmt(sjpencloc, sjpencfieldref);
			c.addLast(assignsjpenc);
			wc.arglist.setElementAt(sjpencloc, position);
		}

		//  ------------ now the other ones ----------------
		// if it has an after returning parameter
		AdviceSpec advicespec = advicedecl.getAdviceSpec();
		if (advicespec instanceof AfterReturningArgAdvice) {
			// we have to fill in the param for the return value
			Formal formal = ((AfterReturningArgAdvice) advicespec).getFormal();
			String formalname = formal.getName();
			AbcType abctype = formal.getType();
			Type formaltype = abctype.getSootType();
			int position = advicedecl.getFormalIndex(formalname);
			debug(
				"After returning formal is at position "
					+ position
					+ " has name "
					+ formalname
					+ " and type "
					+ formaltype);
			// TODO...
		}

		// TODO: need to fill in params for target, args and so on
		boolean alldone = true;
		for (int i = 0; i < wc.arglist.size(); i++)
			alldone = alldone && wc.arglist.get(i) != null;

		if (alldone) {
			Stmt s =
				Jimple.v().newInvokeStmt(
					Jimple.v().newVirtualInvokeExpr(
						aspectref,
						advicemethod,
						wc.arglist));
			c.addLast(s);
			return (c);
		} else
			throw new CodeGenException(
				"case not handled yet in making invoke to "
					+ advicemethod.getName());
	}
}

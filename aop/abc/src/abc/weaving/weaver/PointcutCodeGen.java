package abc.weaving.weaver;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AfterAdvice;
import abc.weaving.aspectinfo.AfterReturningAdvice;
import abc.weaving.aspectinfo.AfterThrowingAdvice;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.BeforeAdvice;
import abc.weaving.aspectinfo.BeforeAfterAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.AlwaysMatch;

public class PointcutCodeGen {

   public static void debug(String message)
     { if (abc.main.Debug.v().pointcutCodeGen) 
          System.err.println("PCG*** " + message);
     }
	public void weaveInAspectsPass(SootClass cl, int pass) {
		for (Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext();) { // get the next method
			final SootMethod method = (SootMethod) methodIt.next();
	
			// nothing to do for abstract or native methods 
			if (method.isAbstract())
				continue;
			if (method.isNative())
				continue;
	
			// get all the advice list for this method
			MethodAdviceList adviceList = GlobalAspectInfo.v().getAdviceList(method);
	
			
	
			switch (pass) {
				case 1 : // ----------------------- PASS 1 -------------
					// if no advice list for this method, nothing to do
					if ((adviceList == null) || (!adviceList.hasBodyAdvice() && !adviceList.hasStmtAdvice())) {
						debug("No body or stmt advice for method " + method.getName());
						continue;
					}
	
					// have something to do ...
					debug("   --- BEGIN weaveInAspectsPass " + pass + " for method " + method.getName());
					Body b = method.getActiveBody();
					LocalGeneratorEx localgen = new LocalGeneratorEx(b);
	
					//	do the stmt advice
					for (Iterator alistIt = adviceList.stmtAdvice.iterator(); alistIt.hasNext();) {
						final AdviceApplication execappl = (AdviceApplication) alistIt.next();
						weave_one(cl, method, localgen, execappl);
					} // each stmt advice
	
					// I pulled this test out of the following loop for efficiency 
					final boolean isAroundMethod=method.getName().startsWith("around$"); // TODO: some proper check
					
					// do the body advice 
					for (Iterator alistIt = adviceList.bodyAdvice.iterator(); alistIt.hasNext();) {
						final AdviceApplication execappl = (AdviceApplication) alistIt.next();
						
						// weaving of adviceexecution for around-advice is delayed
						if (!(isAroundMethod && checkForAroundAdviceExecution(method, execappl)))
							weave_one(cl, method, localgen, execappl);
					} // each body advice 
	
					break;
	
				case 2 : // ----------------------- PASS 2 ----------------
					// if no advice list for this method, nothing to do
					if ((adviceList == null)
						|| (!adviceList.hasInitializationAdvice() && !adviceList.hasPreinitializationAdvice())
						|| // FIXME: shouldn't need this check
					 (!method.getName().equals("<init>"))) {
						debug("No init or preinit advice for method " + method.getName());
						continue;
					}
	
					// have something to do ...
					Body b2 = method.getActiveBody();
					LocalGeneratorEx localgen2 = new LocalGeneratorEx(b2);
	
					// do the init advice 
					for (Iterator alistIt = adviceList.initializationAdvice.iterator(); alistIt.hasNext();) {
						final AdviceApplication initappl = (AdviceApplication) alistIt.next();
						weave_one(cl, method, localgen2, initappl);
					} // each init advice 
	
					// do the preinit advice 
					for (Iterator alistIt = adviceList.preinitializationAdvice.iterator(); alistIt.hasNext();) {
						final AdviceApplication preinitappl = (AdviceApplication) alistIt.next();
						weave_one(cl, method, localgen2, preinitappl);
					} // each preinit advice 
					break;
	
				default : // ------------------------- DEFAULT --------------
					throw new CodeGenException("Undefined pass");
			}
	
			debug("   --- END weaveInAspectsPass " + pass + " for method " + method.getName() + "\n");
		} // each method 
	}
	
	// Stores one AdviceApplication/method pair 
	private static class AroundAdviceExecutionApplication 
			implements Comparable {
				
		AroundAdviceExecutionApplication(SootMethod targetMethod, 
					SootMethod adviceMethod, 
					AdviceApplication appl,
					int originalOrderID) {
			this.targetAdviceMethod=targetMethod;
			this.execappl=appl;
			this.adviceMethod=adviceMethod;
			this.originalOrderID=originalOrderID;
		}
		
		final SootMethod targetAdviceMethod;
		final AdviceApplication execappl;
		final SootMethod adviceMethod;
		//final int original
		public boolean isAlwaysMatchAppl() {
			return execappl.residue instanceof AlwaysMatch;
		}
		
		int orderID=-1;
		final int originalOrderID;
		public int compareTo(Object arg0) {
			AroundAdviceExecutionApplication appl=
				(AroundAdviceExecutionApplication)arg0;
			if (orderID==-1 || appl.orderID==-1)
				throw new RuntimeException();
			
			int result=new Integer(orderID).compareTo(new Integer(appl.orderID));
			if (result==0)
				result=new Integer(originalOrderID).compareTo(new Integer(appl.originalOrderID));
			return result;
		}
		
		
		public String toString() {
			return adviceMethod.getName() + "=>" + targetAdviceMethod.getName() 
				+ " (" + originalOrderID + ")" ;
		}
	}
	
	// id for the sorting algorithm
	private int currentOrderID;
	//
	private void sortAroundAdviceExecutionApplications() {		
		
		Map /*SootMethod, Set<SootMethod> */ graph=new HashMap();
		
		debug("AdviceExecution: old order: *******************");
		// create a graph structure from the applications
		for (Iterator it=aroundAdviceExecutionApplications.iterator();
				it.hasNext();) {
			AroundAdviceExecutionApplication appl=
				(AroundAdviceExecutionApplication)it.next();
			
			debug(" " + appl);
			
			if (!graph.containsKey(appl.adviceMethod))
				graph.put(appl.adviceMethod, new HashSet());
			
			((HashSet)graph.get(appl.adviceMethod)).add(appl.targetAdviceMethod);		
		}
		Set visited=new HashSet();
		Set explored=new HashSet();
		Map /*SootMethod, Integer*/ numbers=new HashMap();
		//Integer ID=new Integer(0);
		currentOrderID=0;
		
		// sort the graph (store a number for each node)
		for (Iterator it=graph.keySet().iterator(); it.hasNext();) {
			SootMethod adviceMethod=(SootMethod)it.next();
			topologicalSort(adviceMethod, graph, visited, explored, numbers);
		}
		
		// set the orderIDs of the applications
		for (Iterator it=aroundAdviceExecutionApplications.iterator();
				it.hasNext();) {
			AroundAdviceExecutionApplication appl=
				(AroundAdviceExecutionApplication)it.next();
			
			appl.orderID=((Integer)numbers.get(appl.targetAdviceMethod)).intValue();
		}	
		
		// sort the applications
		Collections.sort(aroundAdviceExecutionApplications);
		//Collections.reverse(aroundAdviceExecutionApplications);
		
		// debug-output the resulting order
		debug("AdviceExecution: new order: *******************");
		for (Iterator it=aroundAdviceExecutionApplications.iterator();
				it.hasNext();) {
			AroundAdviceExecutionApplication appl=
				(AroundAdviceExecutionApplication)it.next();
		
			debug(" " + appl);
		}
	}
	// recursive sort function
	private void topologicalSort(SootMethod adviceMethod, Map graph, Set visited, 
		Set explored, Map numbers) {
			
		if (explored.contains(adviceMethod))
			return;
		
		if (visited.contains(adviceMethod)) {
			throw new RuntimeException(
				"Semantic error: cyclic graph (adviceexecution): " + // TODO: Fix this message!
				adviceMethod			
			);
		}
		
		visited.add(adviceMethod);
		
		HashSet weavesInto=(HashSet)graph.get(adviceMethod);
		if (weavesInto!=null) {
			for (Iterator it=weavesInto.iterator(); it.hasNext();) {
				SootMethod method=(SootMethod)it.next();
				topologicalSort(method, graph, visited, explored, numbers);
			}
		}
		
		explored.add(adviceMethod);
		numbers.put(adviceMethod, new Integer(currentOrderID++));
		
	}
	
	// Weaves in the around advice applying to around advice methods (adviceexecution).
	// These are woven in last.
	void weaveInAroundAdviceExecutionsPass() {		
		debug("********* Weaving around/adviceexecution");
		
		if (!aroundAdviceExecutionApplications.isEmpty()) {
			try {
				sortAroundAdviceExecutionApplications();
			} catch (Exception e) {
			}
		}
		
		for (Iterator it=aroundAdviceExecutionApplications.iterator();
				it.hasNext();) {
			AroundAdviceExecutionApplication appl=
				(AroundAdviceExecutionApplication)it.next();
			
			LocalGeneratorEx lg=new LocalGeneratorEx(appl.targetAdviceMethod.getActiveBody());
			SootClass cl=appl.targetAdviceMethod.getDeclaringClass();
			weave_one(cl, appl.targetAdviceMethod, lg, appl.execappl);			 
		}
	}
	private List aroundAdviceExecutionApplications=new LinkedList();
	// checks if this combination of method+adviceApplication 
	// is around advice applying to around advice 
	int originalOrderID=0;
	private boolean checkForAroundAdviceExecution(final SootMethod targetMethod, final AdviceApplication execappl) {
		if (AroundWeaver.Util.isAroundAdviceMethod(targetMethod)) {			
			if (execappl.advice instanceof AdviceDecl) {
				AdviceDecl adviceDecl = (AdviceDecl)  execappl.advice;
				AdviceSpec adviceSpec =adviceDecl.getAdviceSpec();
				if (adviceSpec instanceof AroundAdvice) {
					SootMethod adviceMethod = adviceDecl.getImpl().getSootMethod();
					if (!AroundWeaver.Util.isAroundAdviceMethod(adviceMethod))
						throw new RuntimeException("Expecting around advice method names to start with 'around$'");
					
					debug("Advice method " + adviceMethod + " applies to advice method " + targetMethod + "(adviceexecution)");
					
					// remember this application for later
					aroundAdviceExecutionApplications.add(
						new AroundAdviceExecutionApplication(targetMethod, adviceMethod, execappl,
							originalOrderID++));
					return true;
				}
			}							
		}
		return false;
	} 
	
	
	 // method weaveInAspectsPass1   
    private void weave_one( SootClass cl, SootMethod method,
                            LocalGeneratorEx localgen, 
			    AdviceApplication adviceappl) {

	debug("starting weave_one for "+method.getName());
	if(abc.main.Debug.v().pointcutCodeGen) {
	    StringBuffer details=new StringBuffer();
	    adviceappl.debugInfo("PCG: ",details);
	    System.out.println(details.toString());
	}

	adviceappl.advice.getAdviceSpec().weave(method,localgen,adviceappl);
	debug("finished weave_one");

    }  // method weave_one

    // inline everywhere and remove
    public static WeavingContext makeWeavingContext(AdviceApplication adviceappl) {
	return adviceappl.advice.makeWeavingContext();
    }

}

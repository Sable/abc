/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Sascha Kuzins
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.weaver.around.AroundWeaver;
import abc.weaving.weaver.around.Util;

/** Weave in the code for pointcut invocation
 *  @author Laurie Hendren
 *  @author Ondrej Lhotak
 *  @author Jennifer Lhotak
 *  @author Sascha Kuzins
 *  @author Ganesh Sittampalam
 */
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
                        MethodAdviceList adviceList = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceList(method);



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

                                        //      do the stmt advice
                                        for( Iterator execapplIt = adviceList.stmtAdvice.iterator(); execapplIt.hasNext(); ) {
                                            final AdviceApplication execappl = (AdviceApplication) execapplIt.next();
                                                weave_one(cl, method, localgen, execappl);
                                        } // each stmt advice
                                        
                                        final boolean isAroundMethod=method.getName().startsWith("around$"); // TODO: some proper check

                                        // do the body advice
                                        for( Iterator execapplIt = adviceList.bodyAdvice.iterator(); execapplIt.hasNext(); ) {
                                            final AdviceApplication execappl = (AdviceApplication) execapplIt.next();

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
                                        for( Iterator initapplIt = adviceList.initializationAdvice.iterator(); initapplIt.hasNext(); ) {
                                            final AdviceApplication initappl = (AdviceApplication) initapplIt.next();
                                                weave_one(cl, method, localgen2, initappl);
                                        } // each init advice

                                        // do the preinit advice
                                        for( Iterator preinitapplIt = adviceList.preinitializationAdvice.iterator(); preinitapplIt.hasNext(); ) {
                                            final AdviceApplication preinitappl = (AdviceApplication) preinitapplIt.next();
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
                        return execappl.getResidue() instanceof AlwaysMatch;
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
                for( Iterator applIt = aroundAdviceExecutionApplications.iterator(); applIt.hasNext(); ) {
                    final AroundAdviceExecutionApplication appl = (AroundAdviceExecutionApplication) applIt.next();

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
                for( Iterator adviceMethodIt = graph.keySet().iterator(); adviceMethodIt.hasNext(); ) {
                    final SootMethod adviceMethod = (SootMethod) adviceMethodIt.next();
                        topologicalSort(adviceMethod, graph, visited, explored, numbers);
                }

                // set the orderIDs of the applications
                for( Iterator applIt = aroundAdviceExecutionApplications.iterator(); applIt.hasNext(); ) {
                    final AroundAdviceExecutionApplication appl = (AroundAdviceExecutionApplication) applIt.next();

                        appl.orderID=((Integer)numbers.get(appl.targetAdviceMethod)).intValue();
                }

                // sort the applications
                Collections.sort(aroundAdviceExecutionApplications);
                //Collections.reverse(aroundAdviceExecutionApplications);

                // debug-output the resulting order
                debug("AdviceExecution: new order: *******************");
                for( Iterator applIt = aroundAdviceExecutionApplications.iterator(); applIt.hasNext(); ) {
                    final AroundAdviceExecutionApplication appl = (AroundAdviceExecutionApplication) applIt.next();

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
                        for( Iterator methodIt = weavesInto.iterator(); methodIt.hasNext(); ) {
                            final SootMethod method = (SootMethod) methodIt.next();
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

                for( Iterator applIt = aroundAdviceExecutionApplications.iterator(); applIt.hasNext(); ) {

                    final AroundAdviceExecutionApplication appl = (AroundAdviceExecutionApplication) applIt.next();

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
        	
        	 SootMethod targetAdviceMethod=null;
        	 if (Util.isAroundAdviceMethod(targetMethod))
        	 	targetAdviceMethod=targetMethod;
        	 else 
        	 	targetAdviceMethod=AroundWeaver.v().getEnclosingAroundAdviceMethod(targetMethod);
        	
                if (targetAdviceMethod!=null) {                		
                        if (execappl.advice instanceof AdviceDecl) {
                                AdviceDecl adviceDecl = (AdviceDecl)  execappl.advice;
                                AdviceSpec adviceSpec =adviceDecl.getAdviceSpec();
                                if (adviceSpec instanceof AroundAdvice) {
                                        SootMethod adviceMethod = adviceDecl.getImpl().getSootMethod();
                                        if (!Util.isAroundAdviceMethod(adviceMethod))
                                                throw new RuntimeException("Expecting around advice method names to start with 'around$'");

                                        debug("Advice method " + adviceMethod + " applies to advice method " + targetAdviceMethod + "(adviceexecution)");

                                        // remember this application for later
                                        aroundAdviceExecutionApplications.add(
                                                new AroundAdviceExecutionApplication(targetAdviceMethod, adviceMethod, execappl,
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
        { // Debug check
                Chain units=method.getActiveBody().getUnits();
                ShadowPoints sp=adviceappl.shadowmatch.sp;
                if (!units.contains(sp.getBegin())) {
                        debug("method: \n" + Util.printMethod(method));
                        throw new InternalCompilerError(
                                "Appl: " + adviceappl +
                                " Method body of " + method +
                                " does not contain begin shadow point " + sp.getBegin() +
                                " Sp.method: " + sp.getShadowMatch().getContainer() +
                                " == " + (sp.getShadowMatch().getContainer()==method));
                }
                if (!units.contains(sp.getEnd())) {
                        throw new InternalCompilerError("Method body of " + method + " does not contain end shadow point " + sp.getEnd());
                }
        }
        if( abc.main.Debug.v().dumpAAWeave ) {
          System.err.println("weaving " + adviceappl);
          System.err.println("residue is " + adviceappl.getResidue());
        }
        if(!NeverMatch.neverMatches(adviceappl.getResidue()))
            adviceappl.advice.getAdviceSpec().weave(method,localgen,adviceappl);
        debug("finished weave_one");

    }  // method weave_one

    // inline everywhere and remove
    public static WeavingContext makeWeavingContext(AdviceApplication adviceappl) {
        return adviceappl.advice.makeWeavingContext();
    }

}

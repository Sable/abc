/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2005 Pavel Avgustinov
 * Copyright (C) 2006 Eric Bodden
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

package abc.tm.weaving.aspectinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import polyglot.util.Position;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.RefLikeType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import abc.main.Debug;
import abc.soot.util.UnUsedParams;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.CodeGenHelper;
import abc.tm.weaving.weaver.IndexedCodeGenHelper;
import abc.tm.weaving.weaver.itds.ITDAnalysisResults;
import abc.tm.weaving.weaver.itds.ITDOptimisation;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.aspectinfo.GlobalAspectInfo;

/** 
 * Represents a TraceMatch.
 *
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 *  @author Eric Bodden
 */
public class TraceMatch
{
    protected String name;
    protected boolean per_thread;

    protected List formals;
    protected Map formal_name_to_type;
    protected List new_advice_body_formals;
    protected Map param_name_to_body_pos = null;
    protected int[] formal_pos_to_body_pos = new int[0];
    protected int non_around_formals;

    protected StateMachine state_machine;
    protected IndexingScheme indexing_scheme;

    protected List frequent_symbols;
    protected Map sym_to_vars;
    protected Map sym_to_advice_name;
    protected String synch_advice_name;
    protected String some_advice_name;
    protected String dummy_proceed_name;

    protected Aspect container;
 
    protected SootClass constraint = null;
    protected SootClass disjunct = null;
    protected SootClass event = null;
    protected SootClass labels = null;
    protected SootClass labels_thread_local = null;
    protected SootClass tm_weak_ref;
    protected SootClass tm_persistent_weak_ref;
    protected Map primitive_to_box = null;
 
    protected CodeGenHelper helper;
    protected Position position;

    protected Collection unused_formals = null;
	   
    protected Map advice_name_to_sym_name;

    protected static Map idToTracematch = new HashMap();
    
    protected ITDAnalysisResults itdresults = null;

    public TraceMatch(String name, List formals, List new_advice_body_formals,
                        StateMachine state_machine, boolean per_thread,
                        Map sym_to_vars, List frequent_symbols,
                        Map sym_to_advice_name, String synch_advice_name,
                        String some_advice_name, String dummy_proceed_name,
                        Aspect container, Position pos)
    {
        this.name = name;
        this.per_thread = per_thread;

        this.formals = formals;
        this.formal_name_to_type = new HashMap();
        this.new_advice_body_formals = new_advice_body_formals;

        this.state_machine = state_machine;
        this.sym_to_vars = sym_to_vars;
        this.frequent_symbols = frequent_symbols;

        this.sym_to_advice_name = sym_to_advice_name;
        this.synch_advice_name = synch_advice_name;
        this.some_advice_name = some_advice_name;
        this.dummy_proceed_name = dummy_proceed_name;

        this.container = container;
        this.position = pos;

        makeFormalMaps();
        makePrimitiveMaps();

	this.helper = new IndexedCodeGenHelper(this);
        
        idToTracematch.put(name, this);
        
    	// store reverse mapping because for the static TM analysis we
        // need to be able to identify for each woven advice the symbol
        // it was woven for
        advice_name_to_sym_name = new HashMap();
        Iterator iterator = sym_to_advice_name.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            advice_name_to_sym_name.put(entry.getValue(), entry.getKey());
        }
    }

    public static TraceMatch forId(String id)
    {
        TraceMatch res = (TraceMatch) idToTracematch.get(id);
        if (res == null) {
            throw new RuntimeException("No such tracematch: "+id);
        }
        return res;
    }

    public boolean isPerThread()
    {
        return per_thread;
    }

    public boolean isAround()
    {
        return dummy_proceed_name != null;
    }

    public Position getPosition() {
        return position;
    }
	
    public SootClass getContainerClass()
    {
        return container.getInstanceClass().getSootClass();
    }

    public List getFormals()
    {
        return formals;
    }

    public List<String> getNonPrimitiveFormalNames()
    {
        List<String> non_primitive = new ArrayList<String>();
        Iterator i = formals.iterator();

        while (i.hasNext())
        {
            Formal f = (Formal) i.next();
            if (! isPrimitive(f.getName()))
                non_primitive.add(f.getName());
        }

        return non_primitive;
    }
    
    public List<String> getFormalNames()
    {
        List<String> formalNames = new LinkedList<String>();
        for (Iterator formalIter = formals.iterator(); formalIter.hasNext(); )
        {
            Formal f = (Formal) formalIter.next();
            formalNames.add(f.getName());
        }
        return formalNames;
    }

    public List getNewAdviceBodyFormals()
    {
        return new_advice_body_formals;
    }

    protected void makeFormalMaps()
    {
        param_name_to_body_pos = new HashMap();

        for (int i = 0; i < new_advice_body_formals.size(); i++)
        {
            Formal f = (Formal) new_advice_body_formals.get(i);

            param_name_to_body_pos.put(f.getName(), new Integer(i));
        }

        Set thisjp_vars = new HashSet(param_name_to_body_pos.keySet());

        formal_pos_to_body_pos = new int[formals.size() + 3];

        for (int i = 0; i < formals.size(); i++) {
            Formal f = (Formal) formals.get(i);

            thisjp_vars.remove(f.getName());

            formal_name_to_type.put(f.getName(), f.getType().getSootType());
            formal_pos_to_body_pos[i] = getBodyParameterIndex(f.getName());
        }

        int offset = new_advice_body_formals.size() - thisjp_vars.size();

        for (int i = 0; i < 3; i++)
            formal_pos_to_body_pos[i + formals.size()] = i + offset;

        non_around_formals = formals.size() - offset;
    }
    
    public int getBodyParameterIndex(String name)
    {
        if (!param_name_to_body_pos.containsKey(name))
            return -1;

        return ((Integer) param_name_to_body_pos.get(name)).intValue();
    }

    public int getBodyParameterIndex(int formal_pos)
    {
        return formal_pos_to_body_pos[formal_pos];
    }

    public int nonAroundFormals()
    {
        return non_around_formals;
    }

    public StateMachine getStateMachine() {
        return state_machine;
    }

    public Set<String> getSymbols() {
        return sym_to_vars.keySet();
    }

    public Collection<String> getFrequentSymbols()
    {
        return frequent_symbols;
    }

    public List<String> getVariableOrder(String symbol)
    {
        return (List<String>) sym_to_vars.get(symbol);
    }

    public void createIndexingScheme()
    {
        indexing_scheme = new IndexingScheme(this);
    }

    public IndexingScheme getIndexingScheme()
    {
        return indexing_scheme;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Get the name of the package that contains the aspect which
     * contains this tracematch.
     *
     * (includes the trailing full-stop)
     */
    public String getPackage()
    {
        String jvm_name = container.getInstanceClass().getJvmName();

        int package_length = jvm_name.lastIndexOf('.') + 1;

        return jvm_name.substring(0, package_length);
    }

    public SootMethod getBodyMethod()
    {
        String body_name = getName() + "$body";

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(body_name);

        return sm;
    }

    public SootMethod getRealBodyMethod()
    {
        String body_name = getName() + "$body_real";

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(body_name);

        return sm;
    }

    public SootMethod getSymbolAdviceMethod(String symbol)
    {
        String advice_name = (String) sym_to_advice_name.get(symbol);

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(advice_name);

        return sm;
    }

    public SootMethod getSynchAdviceMethod()
    {
        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(synch_advice_name);

        return sm;
    }

    public SootMethod getSomeAdviceMethod()
    {
        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(some_advice_name);

        return sm;
    }

    public String getDummyProceedName()
    {
        return dummy_proceed_name;
    }

    public void setConstraintClass(SootClass constraint)
    {
        this.constraint = constraint;
        helper.setConstraintClass(constraint);
    }

    public SootClass getConstraintClass()
    {
        return constraint;
    }

    public void setDisjunctClass(SootClass disjunct)
    {
        this.disjunct = disjunct;
        helper.setDisjunctClass(disjunct);
    }

    public SootClass getDisjunctClass()
    {
        return disjunct;
    }

    public void setEventClass(SootClass event)
    {
        this.event = event;
        helper.setEventClass(event);
    }

    public SootClass getEventClass()
    {
        return event;
    }

    public void setLabelsClass(SootClass labels)
    {
        this.labels = labels;
    }

    public SootClass getLabelsClass()
    {
        if (isPerThread())
            return labels;
        else
            return getContainerClass();
    }

    public void setLabelsThreadLocalClass(SootClass labels_thread_local)
    {
        this.labels_thread_local = labels_thread_local;
    }

    public SootClass getLabelsThreadLocalClass()
    {
        return labels_thread_local;
    }

    public CodeGenHelper getCodeGenHelper()
    {
        return helper;
    }

    public void findUnusedFormals()
    {
        unused_formals =
            UnUsedParams.unusedFormals(getBodyMethod(), getFormalNames());
    }

    public Collection getUnusedFormals()
    {
        return unused_formals;
    }

    public boolean isPrimitive(String name)
    {
        Type type = bindingType(name);
        
        return ! (type instanceof RefLikeType);
    }

    public Type bindingType(String name)
    {
        return (Type) formal_name_to_type.get(name);
    }

    public SootClass weakBindingClass(String name)
    {
        Type type = bindingType(name);

        if (type instanceof RefLikeType)
            return tm_weak_ref;
        else
            return (SootClass) primitive_to_box.get(type);
    }
    
    public SootClass persistentWeakRefClass() {
    	return tm_persistent_weak_ref;
    }

    public Type weakBindingConstructorArgType(String name)
    {
        Type type = bindingType(name);

        if (type instanceof RefLikeType)
            return Scene.v().getRefType("java.lang.Object");
        else
            return type;
    }

    protected void makePrimitiveMaps()
    {
        primitive_to_box = new HashMap();

        primitive_to_box.put(BooleanType.v(),
                             Scene.v().getSootClass("java.lang.Boolean"));
        primitive_to_box.put(ByteType.v(),
                             Scene.v().getSootClass("java.lang.Byte"));
        primitive_to_box.put(CharType.v(),
                             Scene.v().getSootClass("java.lang.Character"));
        primitive_to_box.put(DoubleType.v(),
                             Scene.v().getSootClass("java.lang.Double"));
        primitive_to_box.put(FloatType.v(),
                             Scene.v().getSootClass("java.lang.Float"));
        primitive_to_box.put(IntType.v(),
                             Scene.v().getSootClass("java.lang.Integer"));
        primitive_to_box.put(LongType.v(),
                             Scene.v().getSootClass("java.lang.Long"));
        primitive_to_box.put(ShortType.v(),
                             Scene.v().getSootClass("java.lang.Short"));


        if(abc.main.Debug.v().clashHashCodes) {
            tm_weak_ref = Scene.v().getSootClass(
                "org.aspectbench.tm.runtime.internal.ClashWeakRef");
            tm_persistent_weak_ref = Scene.v().getSootClass(
                "org.aspectbench.tm.runtime.internal.ClashPersistentWeakRef");
        } else {
            tm_weak_ref = Scene.v().getSootClass(
                "org.aspectbench.tm.runtime.internal.MyWeakRef");
            tm_persistent_weak_ref = Scene.v().getSootClass(
                "org.aspectbench.tm.runtime.internal.PersistentWeakRef");
        }
    }

    protected SootMethod getConstructor(SootClass constructed, Type param)
    {
        String init_name = SootMethod.constructorName;
        List type_list = new LinkedList();
        type_list.add(param);

        return constructed.getMethod(init_name, type_list);
    }
    
    /**
     * For a given symbol advice method, returns the name of the symbol it was
     * generated for.
     * @param symbolAdviceMethod a symbol advice method
     * @return the name of the symbol this symbol advice method was generated
     * for or * <code>null</code> if the name is not the name of a symbol
     * advice for this tracematch
     */
    public String symbolNameForSymbolAdvice(SootMethod symbolAdviceMethod)
    {
        return (String)
            advice_name_to_sym_name.get(symbolAdviceMethod.getName());
    }
		
    /**
     * Implements the <i>quick check</i> as described in the ECOOP '07 paper.
     * Removes all the symbols from the tracematch alphabet for which
     * the related per-symbol advice never matched during the last weaving
     * phase.
     * @return <code>true</code> if it is safe to remove this tracematch
     * entirely because no final state can be reached any more
     */
    public boolean quickCheck()
    {
        GlobalAspectInfo gai =
            abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
        TMStateMachine sm = (TMStateMachine) getStateMachine();
		
        // determine all symbols for which there exists a per-symbol advice
        // declaration with an application counter of more than 0 ; such
        // symbols need to be retained
        Set symbolsToRetain = new HashSet();
		
        // for all advice declarations
        for (Iterator iter = gai.getAdviceDecls().iterator(); iter.hasNext();) {
            AbstractAdviceDecl ad = (AbstractAdviceDecl) iter.next();

            // of kind "per-symbol advice declaration"
            if(ad instanceof PerSymbolTMAdviceDecl) {
                PerSymbolTMAdviceDecl psad = (PerSymbolTMAdviceDecl) ad;
				
                // for this tracematch
                if(psad.getTraceMatchID().equals(name))
                {
                    // if this advice was never applied
                    if(psad.getApplCount()>0)
                    {
                        // mark symbol as a symbol that must be retained
                        symbolsToRetain.add(psad.getSymbolId());
                    }
                }
            }
        }
		
        // all symbols which do not have to be retained can be removed
        Set symbolsToRemove = new HashSet(sym_to_vars.keySet());
        symbolsToRemove.removeAll(symbolsToRetain);
		
        if(symbolsToRemove.size()>0 && Debug.v().debugTmAnalysis) {
            System.out.println("The following symbols do not match and " +
                                "will be removed in " + name + ": " +
                                symbolsToRemove);
        }
		
        Set edgesToRemove = new HashSet();
        // remove all edges for those symbols, including skip-loops
        // labelled that way
        Iterator symIter = symbolsToRemove.iterator();
        while (symIter.hasNext()) {
            String symbolName = (String) symIter.next();
	
            // for all edges in the state machine
            Iterator iterator = sm.getEdgeIterator();
            while (iterator.hasNext()) {
                SMEdge edge = (SMEdge) iterator.next();

                // with the same symbol name as the advice that never applied 
                if(edge.getLabel().equals(symbolName)) {
                    // mark edge as to be removed
                    edgesToRemove.add(edge);
                }
            }
        }
        // remove them
        sm.removeEdges(edgesToRemove);
        //compress the states (eliminate unreachable states)
        sm.compressStates();
	
        //is the SM now empty?
        boolean isEmpty = !sm.getStateIterator().hasNext();
		
        if(Debug.v().debugTmAnalysis && symbolsToRemove.size() > 0 && !isEmpty)
        {
            System.out.println("Remaining state machine:");
            System.out.println(sm);
        }
	
        if(isEmpty) {
            // if the state machine is now empty, remove this
            // tracematch entirely
            if(Debug.v().debugTmAnalysis) {
                System.out.println("State machine now empty. Removing " +
                                    name + ".");
            }
            return true;
        } else {
            // else remove all the symbols which don't match from the
            // tracematch; this will also remove the handling for skip
            // loops for those symbols,
            // which is safe cause we know the symbol can never occur anyway
	
            Iterator symbolIter = symbolsToRemove.iterator();
            while (symbolIter.hasNext()) {
                String symbolName = (String) symbolIter.next();
                sym_to_advice_name.remove(symbolName);
                sym_to_vars.remove(symbolName);
            }
	
            if(Debug.v().debugTmAnalysis && symbolsToRemove.size() > 0)
            {
                System.out.println("Remaining alphabet of " + name +
                                   ": " + getSymbols());
                System.out.println(sm);
            }
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+" +
                  "-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n");
        sb.append("Tracematch '" + getName() +"' in aspect '" +
                  container.getName() + "':\n\n");
        sb.append("  position:      " + getPosition() + "\n");
        sb.append("  formals:       " + getFormals() + "\n");
        sb.append("  symbols:       " + sym_to_vars + "\n");
        sb.append("  frequent sym:  " + getFrequentSymbols() + "\n");
        sb.append("  is around:     " + isAround() + "\n");
        sb.append("  is perthread:  " + isPerThread() + "\n");
        sb.append(" - - - - - - - - - - - - - - - - - - " +
                  "- - - - - - - - - - - - - - - - - - \n");
        sb.append(getStateMachine());
        sb.append("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+" +
                  "-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n");
        return sb.toString();
    }
	
    public static void reset()
    {
        idToTracematch.clear();
    }

    public boolean hasITDAnalysisResults()
    {
        return itdresults != null;
    }

    public ITDAnalysisResults getITDAnalysisResults()
    {
        if (itdresults == null)
            itdresults = new ITDAnalysisResults(this);
        return itdresults;
    }

    public void doITDOptimisation()
    {
        new ITDOptimisation(this).doITDOptimisation();
    }

    public Set<String> getInitialSymbols()
    {
        Set<String> initial_symbols = new HashSet<String>();
        TMStateMachine sm = ((TMStateMachine) getStateMachine()); 
        for (SMNode initial_state : sm.getInitialStates()) {
            Iterator i = initial_state.getOutEdgeIterator();
            while (i.hasNext()) {
                SMEdge edge = (SMEdge) i.next();
                initial_symbols.add(edge.getLabel());
            }
        }
        return initial_symbols;
    }
    
    public Set<String> getFinalSymbols()
    {
        Set<String> final_symbols = new HashSet<String>();
        TMStateMachine sm = ((TMStateMachine) getStateMachine());
        for (Iterator<State> stateIter = sm.getStateIterator(); stateIter.hasNext();) {
			State s = stateIter.next();
			if(s.isFinalNode()) {
				SMNode node = (SMNode) s;
				for (Iterator<SMEdge> inEdgeIter = node.getInEdgeIterator(); inEdgeIter.hasNext();) {
					SMEdge inEdge = inEdgeIter.next();
	                final_symbols.add(inEdge.getLabel());
				}
			}
		}
        return final_symbols;
    }

    public int getInitialStateNumber()
    {
        TMStateMachine sm = (TMStateMachine) getStateMachine();
        Iterator<SMNode> states = sm.getStateIterator();
        while (states.hasNext()) {
            SMNode state = states.next();
            if (state.isInitialNode())
                return state.getNumber();
        }
        throw new RuntimeException("can't find initial state");
    }

    public int getFinalStateNumber()
    {
        TMStateMachine sm = (TMStateMachine) getStateMachine();
        Iterator<SMNode> states = sm.getStateIterator();
        while (states.hasNext()) {
            SMNode state = states.next();
            if (state.isFinalNode())
                return state.getNumber();
        }
        throw new RuntimeException("can't find final state");
    }

	public Aspect getContainer() {
		return container;
	}
}

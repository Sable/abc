/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
 
package abc.da.weaving.aspectinfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import abc.da.HasDAInfo;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SimpleStateMachine;
import abc.tm.weaving.matching.StateMachine;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;

/**
 * A {@link TracePattern} declared in source code.
 * @author Eric Bodden
 */
public class TracePatternFromSource implements TracePattern {

	protected SimpleStateMachine sm;	
	protected Aspect container;
	protected String name;
	protected Map<String,List<String>> symToVars; 
	protected Map<String,SootMethod> symToAdviceMethod;
	protected Set<String> formals;
		
	public TracePatternFromSource(
			Aspect container,
			Map<String, List<String>> symToVars,
			SimpleStateMachine sm,
			String name) {
		this.container = container;
		this.symToVars = symToVars;
		this.name = name;
		this.formals = new HashSet<String>();
		for (List<String> vars : symToVars.values()) {
			formals.addAll(vars);
		}
		
		this.sm = sm;
		this.sm.prepare(formals,symToVars);
	}

	public Aspect getContainer() {
		return container;
	}

	public SootClass getContainerClass() {
		return container.getInstanceClass().getSootClass();
	}

    public Set<String> getInitialSymbols()
    {
        Set<String> initial_symbols = new HashSet<String>();
        StateMachine sm = getStateMachine();
        for(Iterator<SMEdge> edgeIter = sm.getEdgeIterator(); edgeIter.hasNext();) {
        	SMEdge edge = edgeIter.next();
        	if(edge.getSource().isInitialNode() && !edge.isSkipEdge()) {
        		initial_symbols.add(edge.getLabel());
        	}
        }        
        return initial_symbols;
    }
    
    public Set<String> getFinalSymbols()
    {
        Set<String> final_symbols = new HashSet<String>();
        StateMachine sm = getStateMachine();
        for(Iterator<SMEdge> edgeIter = sm.getEdgeIterator(); edgeIter.hasNext();) {
        	SMEdge edge = edgeIter.next();
        	if(edge.getTarget().isFinalNode() && !edge.isSkipEdge()) {
        		final_symbols.add(edge.getLabel());
        	}
        }        
        return final_symbols;
    }
    
	public String getName() {
		return name;
	}

	public SimpleStateMachine getStateMachine() {
		return sm;
	}

	public SootMethod getSymbolAdviceMethod(String symbol) {
		if(symToAdviceMethod == null) 
			computeAdviceMethods();
        SootMethod sootMethod = symToAdviceMethod.get(symbol);
        assert sootMethod != null;
		return sootMethod;
	}
	
	protected void computeAdviceMethods() {
	    GlobalAspectInfo gai =
	    	abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();

		DAInfo dai = ((HasDAInfo) abc.main.Main.v().getAbcExtension()).getDependentAdviceInfo();
		
		symToAdviceMethod = new HashMap<String, SootMethod>();
		
		for (AbstractAdviceDecl adviceDecl : gai.getAdviceDecls()) {
			if(adviceDecl.getAspect().equals(container)) {
				if(dai.isDependentAdvice(adviceDecl)) {
					String adviceName = dai.qualifiedNameOfAdvice((AdviceDecl) adviceDecl);
					String givenName = dai.replaceForHumanReadableName(adviceName);
					assert adviceName!=null && givenName!=null;
					adviceName = adviceName.substring(adviceName.lastIndexOf(".")+1); //remove qualification
					givenName  = givenName.substring(givenName.lastIndexOf(".")+1); //remove qualification
					
			        SootClass sc = container.getInstanceClass().getSootClass();
			        SootMethod adviceMethod = sc.getMethodByName(adviceName);
			        symToAdviceMethod.put(givenName, adviceMethod);
				}
			}
		}
	}
	

	public Set<String> getSymbols() {
		return symToVars.keySet();
	}

	public List<String> getVariableOrder(String symbol) {
		return symToVars.get(symbol);
	}

	public Collection<String> getFormals() {
		return formals;
	}

}

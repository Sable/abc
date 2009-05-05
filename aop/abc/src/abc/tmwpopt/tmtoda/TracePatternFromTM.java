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

package abc.tmwpopt.tmtoda;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.SimpleStateMachine;
import abc.weaving.aspectinfo.Aspect;

/**
 * A {@link TracePattern} created from a {@link TraceMatch}. This has special properties, such as
 * suffix-matching semantics.
 * @author Eric Bodden
 */
public class TracePatternFromTM implements TracePattern {

	protected TraceMatch tm;
	protected SimpleStateMachine sm;
	public TracePatternFromTM(TraceMatch tm) {
		this.tm = tm;
		
		sm = ((SimpleStateMachine) tm.getStateMachine()).determinise();  //make copy so that we don't tamper with the original

		//add looping transitions to initial state
		for (SMNode s : sm.getInitialStates()) {
			for (String symbol : getSymbols()) {
				sm.newTransition(s,s,symbol);
			}
		}
		
        Map<String, Collection<String>> symToVars = new HashMap<String, Collection<String>>();
        for(String sym: getSymbols()) {
        	symToVars.put(sym,getVariableOrder(sym));
        }        
		sm.prepare(getFormals(),symToVars);
	}

	public Aspect getContainer() {
		return tm.getContainer();
	}

	public SootClass getContainerClass() {
		return tm.getContainerClass();
	}

	public Set<String> getFinalSymbols() {
		return tm.getFinalSymbols();
	}

	public Set<String> getInitialSymbols() {
		return tm.getInitialSymbols();
	}

	public String getName() {
		return tm.getName();
	}

	public SootMethod getSymbolAdviceMethod(String symbol) {
		return tm.getSymbolAdviceMethod(symbol);
	}

	public Set<String> getSymbols() {
		return tm.getSymbols();
	}

	public List<String> getVariableOrder(String symbol) {
		return tm.getVariableOrder(symbol);
	}

	public SimpleStateMachine getStateMachine() {
		return sm;
	}

	public Collection<String> getFormals() {
		return tm.getFormalNames();
	}
	
}

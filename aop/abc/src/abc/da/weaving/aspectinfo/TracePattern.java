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
import java.util.List;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SimpleStateMachine;
import abc.tm.weaving.matching.StateMachine;
import abc.weaving.aspectinfo.Aspect;

/**
 * A trace pattern is an abstraction of a {@link TraceMatch}.
 * It basically combines a {@link StateMachine} with an alphabet of symbols.
 * We do not make {@link TraceMatch} implement {@link TracePattern} because
 * that would make {@link TraceMatch} dependent on the da extension, which we want to avoid.
 * @author Eric Bodden
 */
public interface TracePattern {
	
	public SootMethod getSymbolAdviceMethod(String symbol);
	
	public List<String> getVariableOrder(String symbol);
	
	public Set<String> getSymbols();

	public Set<String> getInitialSymbols();
	
	public Set<String> getFinalSymbols();

	public Aspect getContainer();
	
	public SootClass getContainerClass();

	public String getName();
	
	public SimpleStateMachine getStateMachine();
	
	public Collection<String> getFormals();
}

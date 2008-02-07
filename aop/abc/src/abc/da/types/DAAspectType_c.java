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
package abc.da.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Formal;
import polyglot.frontend.Source;
import polyglot.types.LazyClassInitializer;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import abc.aspectj.types.AspectType_c;
import abc.da.ast.AdviceName;

/**
 * A type node for an aspect that can hold dependent advice.
 * @author Eric Bodden
 */
public class DAAspectType_c extends AspectType_c implements DAAspectType {

	protected Map<AdviceName,List<Formal>> adviceNameToFormals;
	protected Set<String> referencedAdviceNames;
	
	public DAAspectType_c(TypeSystem ts,
			LazyClassInitializer init, Source fromSource, int perKind) {
		super(ts,init,fromSource,perKind);
		adviceNameToFormals = new HashMap<AdviceName,List<Formal>>();
		referencedAdviceNames = new HashSet<String>();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void checkDuplicateAdviceNames() throws SemanticException {
		for (AdviceName adviceName : adviceNameToFormals.keySet()) {
			for (AdviceName adviceName2 : adviceNameToFormals.keySet()) {
				if(adviceName==adviceName2)
					break;
				if(adviceName.getName().equals(adviceName2.getName())) {
					//take the advice name that is later in textual order;
					//for consistent error messages in the harness
					if(adviceName.position().line()<adviceName2.position().line()) {
						adviceName = adviceName2;						
					}					
					throw new SemanticException("Duplicate advice name: "+adviceName.getName(),adviceName.position());
				}
			}
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void setAdviceNameToFormals(Map<AdviceName,List<Formal>> adviceNameToFormals) {
		this.adviceNameToFormals = adviceNameToFormals;
	}

	/** 
	 * {@inheritDoc}
	 */
	public Map<AdviceName,List<Formal>> getAdviceNameToFormals() {
		return adviceNameToFormals;
	}

	/** 
	 * {@inheritDoc}
	 */
	public void addReferencedAdviceNames(Set<String> adviceNames) {
		referencedAdviceNames.addAll(adviceNames);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Set<String> getAllReferencedAdviceNames() {
		return new HashSet<String>(referencedAdviceNames);
	}

}

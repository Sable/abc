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
package abc.da.ast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polyglot.ast.Formal;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;

/**
 * A combination of an advice name and a vector parameters (variable names).
 * @author Eric Bodden
 */
public class AdviceNameAndParams_c extends Node_c implements AdviceNameAndParams {

	protected final String name;
	protected final boolean defaultParams;
	protected List<String> argumentOrder;

	public AdviceNameAndParams_c(Position pos, String name, List<String> argumentOrder) {
		super(pos);
		this.name = name;
		this.argumentOrder = argumentOrder;
		this.defaultParams = argumentOrder.isEmpty();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/** 
	 * {@inheritDoc}
	 */
	public AdviceNameAndParams defaultParams(Map<AdviceName, List<Formal>> adviceNameToFormals) {
		//argument order given manually
		if(!argumentOrder.isEmpty()) return this;
		List<Formal> found = findFormalsForAdviceName(adviceNameToFormals);
		if(found==null) {
			//the advice name refers to a non-existing advice;
			//just return this, as the type checker will catch the error later on anyway
			return this;
		} else {
			AdviceNameAndParams_c copy = (AdviceNameAndParams_c) copy();
			copy.argumentOrder = new LinkedList<String>();
			//add default arguments
			for (Formal formal : found) {
				copy.argumentOrder.add(formal.name());
			}	
			return copy;
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	public List<Formal> findFormalsForAdviceName(Map<AdviceName, List<Formal>> adviceNameToFormals) {
		List<Formal> found = null;
		for (AdviceName an : adviceNameToFormals.keySet()) {
			if(an.getName().equals(name)) {
				found = adviceNameToFormals.get(an);
				break;
			}
		}
		return found;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List<String> getParams() {
		return new LinkedList<String>(argumentOrder);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        w.write(getName());
        
        if (! argumentOrder.isEmpty()) {
            w.write("<");

            for (Iterator<String> i = argumentOrder.iterator(); i.hasNext(); ) {
                String var = i.next();
                w.write(var);
        
                if (i.hasNext()) {
                    w.write(",");
                    w.allowBreak(0, 4, "",0);
                }
            }
            
            w.write(">");
        }
	}

	/** 
	 * {@inheritDoc}
	 */
	public boolean hasInferredParams() {
		return defaultParams;
	}
	
}

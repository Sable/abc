/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra.weaving.aspectinfo;

import java.util.List;
import java.util.Map;

import polyglot.util.Position;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.StateMachine;
import abc.weaving.aspectinfo.Aspect;

/**
 * A tracematch generated from a relational tracematch.
 * @author Eric Bodden
 */
public class RATraceMatch extends TraceMatch {

	/** Name of the state variable. */
	protected final String stateVarName;

	public RATraceMatch(String name, List formals,
			List new_advice_body_formals, StateMachine state_machine,
			boolean per_thread, Map sym_to_vars, List frequent_symbols,
			Map sym_to_advice_name, String synch_advice_name,
			String some_advice_name, String dummy_proceed_name,
			Aspect container, Position pos, String stateVarName) {
		super(name, formals, new_advice_body_formals, state_machine, per_thread,
				sym_to_vars, frequent_symbols, sym_to_advice_name, synch_advice_name,
				some_advice_name, dummy_proceed_name, container, pos);
		this.stateVarName = stateVarName;
	}
	
	/** 
	 * Same as {@link TraceMatch}{@link #findUnusedFormals()}, but the state variable is not considered
	 * unused, because it is always implicitly used in the body.
	 */
	public void findUnusedFormals() {
		super.findUnusedFormals();
		unused_formals.remove(stateVarName);
	}
	

}

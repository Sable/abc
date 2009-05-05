/* abc - The AspectBench Compiler
 * Copyright (C) 2009 Eric Bodden
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
 
package abc.da.weaving.weaver.tracing;

import java.util.Map;

import abc.da.weaving.aspectinfo.TracePattern;

public class Event {
	
	protected TracePattern tp;
	protected String symbol;
	protected Map<String, Integer> variableToObjectID;
	protected int shadowId;
	protected int num;
	
	public TracePattern getTracePattern() {
		return tp;
	}

	public String getSymbol() {
		return symbol;
	}

	public Map<String, Integer> getVariableBinding() {
		return variableToObjectID;
	}

	public int getShadowId() {
		return shadowId;
	}

	public Event(int num, TracePattern tp, String symbol,
			Map<String, Integer> variableToObjectID, int shadowId) {
		this.num = num;
		this.tp = tp;
		this.symbol = symbol;
		this.variableToObjectID = variableToObjectID;
		this.shadowId = shadowId;
	}
	
	@Override
	public String toString() {
		return num+": "+tp.getName()+"."+symbol+"("+variableToObjectID+") @ "+shadowId;
	}

	public boolean canBindTo(String var, Integer objectId) {
		Integer integer = variableToObjectID.get(var);
		return integer==null || integer.equals(objectId);
	}
	

}

/* abc - The AspectBench Compiler
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
package abc.da.weaving.weaver.dynainstr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import abc.da.weaving.weaver.depadviceopt.ds.Shadow;

/**
 * Probe - similar to {@link ShadowGroup} but keeps a <i>set</i> of shadows only. 
 *
 * @author Eric Bodden
 */
public class Probe {
	
	protected Set<Shadow> shadows; 
	
	protected int number;
	
	public Probe(Collection<Shadow> shadowSet) {
		this.shadows = new HashSet<Shadow>();
		this.shadows.addAll(shadowSet);
		this.shadows = Collections.unmodifiableSet(shadows);
		this.number = -1;
	}
	
	public int getNumber() {
		if(this.number==-1) {
			throw new RuntimeException("number not yet assigned!");
		}
		return number;
	}
	
	public void assignNumber(int probeNumber) {
		if(this.number>-1) {
			throw new RuntimeException("number already assigned!");
		}
		this.number = probeNumber;
	}

	public Set<Shadow> getShadows() {
		return shadows;
	}
	
	@Override
	public String toString() {
		return "Probe "+number+":\n"+shadows;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		result = prime * result + ((shadows == null) ? 0 : shadows.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Probe other = (Probe) obj;
		if (number != other.number)
			return false;
		if (shadows == null) {
			if (other.shadows != null)
				return false;
		} else if (!shadows.equals(other.shadows))
			return false;
		return true;
	}
	
	

}

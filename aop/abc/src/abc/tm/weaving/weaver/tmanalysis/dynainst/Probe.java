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
package abc.tm.weaving.weaver.tmanalysis.dynainst;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;

/**
 * Probe - similar to {@link ShadowGroup} but keeps a <i>set</i> of shadows only. 
 *
 * @author Eric Bodden
 */
public class Probe {
	
	private static int nextProbeNumber;
	
	protected Set shadows; 
	
	protected int number;
	
	private Probe(Collection shadowSet) {
		shadows = new HashSet();
		shadows.addAll(shadowSet);
		shadows = Collections.unmodifiableSet(shadows);
		number = nextProbeNumber++;
	}
	
	public int getNumber() {
		return number;
	}

	public Set getShadows() {
		return shadows;
	}
	
	protected static Set allSoundProbes = null;
	
	public static Set generateAllSoundProbes() {
		if(allSoundProbes!=null) {
			return allSoundProbes;
		}		
		
		nextProbeNumber = 0;
		
		Set shadowSets = new HashSet();
		
		Set allConsistentShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (Iterator groupIter = allConsistentShadowGroups.iterator(); groupIter
				.hasNext();) {
			ShadowGroup sg = (ShadowGroup) groupIter.next();
			shadowSets.add(sg.getAllShadows());
		}
		
		Set probes = new HashSet();
		for (Iterator shadowSetsIter = shadowSets.iterator(); shadowSetsIter.hasNext();) {
			Collection shadowSet = (Collection) shadowSetsIter.next();
			probes.add(new Probe(shadowSet));
		}
		
		allSoundProbes = probes;
		
		return probes;
	}

}

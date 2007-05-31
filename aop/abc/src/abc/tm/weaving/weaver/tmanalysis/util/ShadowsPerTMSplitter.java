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
package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import abc.tm.weaving.weaver.tmanalysis.query.Shadow;

/**
 * This class splits a given set of shadows into multiple sets so that each set holds only shadows of one tracematch. 
 *
 * @author Eric Bodden
 */
public class ShadowsPerTMSplitter {
	
	/**
	 * Splits the shadows in the given set per tracematch.
	 * @param shadows a set of {@link Shadow}s
	 * @return a mapping from tracematch name ({@link String}) to a {@link Set} of {@link Shadow}s of that tracematch
	 */
	public static Map splitShadows(Collection shadows) {
		Map tmNameToShadows = new HashMap();
		
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			
			String uniqueShadowId = shadow.getUniqueShadowId();
			String tracematchName = Naming.getTracematchName(uniqueShadowId);
			
			Set shadowsForTm = (Set) tmNameToShadows.get(tracematchName);
			if(shadowsForTm==null) {
				shadowsForTm = new HashSet();
				tmNameToShadows.put(tracematchName, shadowsForTm);
			}
			
			shadowsForTm.add(shadow);
		}

		return tmNameToShadows;		
	}

}

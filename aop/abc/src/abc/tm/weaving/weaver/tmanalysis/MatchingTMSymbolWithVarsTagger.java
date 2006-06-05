/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import abc.tm.weaving.aspectinfo.TMPerSymbolAdviceDecl;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.tagkit.InstructionShadowTag;

/**
 * @author Eric Bodden
 */
public class MatchingTMSymbolWithVarsTagger extends MatchingTMSymbolTagger {
	
	/** 
	 * {@inheritDoc}
	 */
	protected MatchingTMSymbolTag createTag(InstructionShadowTag tag, List adviceApplications) {
		Map res = new HashMap(); 
		
		//add the symbols IDs for all TMPerSymbolAdviceDecl whose shadow id
		//matches the one of the tag
		for (Iterator iter = adviceApplications.iterator(); iter.hasNext();) {
		    AdviceApplication aa = (AdviceApplication) iter.next();
		    
		    if((aa.shadowmatch.shadowId == tag.value())
		        && (aa.advice instanceof TMPerSymbolAdviceDecl)) {
		        TMPerSymbolAdviceDecl tmAdvice = (TMPerSymbolAdviceDecl) aa.advice;
		        String symbolId = tmAdvice.getQualifiedSymbolId();
		        Map freeVariables = tmAdvice.getFreeVariables();
		        if(!freeVariables.isEmpty()) {
			    	res.put(symbolId, freeVariables);
		        }		        
		    }
		}
		if(!res.isEmpty()) {
			return new MatchingTMSymbolWithVarsTag(res);
		} else {
			return EMPTY_TAG;
		}
	}


}

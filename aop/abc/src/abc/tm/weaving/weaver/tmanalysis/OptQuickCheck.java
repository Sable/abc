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
package abc.tm.weaving.weaver.tmanalysis;

import abc.main.AbcTimer;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.stages.QuickCheck;
import abc.tm.weaving.weaver.tmanalysis.util.Statistics;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * A reweaving analysis that executes the quick-check
 * as described in our ECOOP 2007 paper.
 * @author Eric Bodden
 */
public class OptQuickCheck extends AbstractReweavingAnalysis {

	protected TMGlobalAspectInfo gai;
	
    public boolean analyze() {
    	gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		//nothing to do?
    	if(gai.getTraceMatches().size()==0) {
    		return false;
    	}
    	
    	try {
    		doAnalyze();
    	} catch (Error e) {
    		Statistics.errorOccured = true;
    		throw e;
    	} catch (RuntimeException e) {
    		Statistics.errorOccured = true;
    		throw e;
    	}
    	
		//we do not need to reweave right away
        return false;
    }

    /**
	 * Performs the actual analysis.
	 */
	protected void doAnalyze() {
		AbcTimer.mark("TMAnalysis (prelude)");

		//this performs a quick test that can always be applied:
    	//we see if actually all of the per-symbol advice matched at some point;
    	//if one of them did not match, we remove all edges that
    	//hold this symbol; also, if then the final state becomes unreachable,
    	//we remove the tracematch entirely
    	QuickCheck.v().apply();

    	AbcTimer.mark("Quick check");
    	
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void cleanup() {		
		QuickCheck.reset();
	}


}

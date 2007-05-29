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
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.util.IdentityHashSet;
import abc.main.AbcTimer;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;

/**
 * This check determines if a tracematch in the system can ever match, solely on the number of advice applications
 * that occured during weaving.
 *
 * @author Eric Bodden
 */
public class QuickCheck extends AbstractAnalysisStage {

	/** the set of tracematches that were determined to be non-matching and can hence be disabled */
	protected transient Set traceMatchesToDisable;
	
	/**
	 * {@inheritDoc}
	 */
	public void doAnalysis() {
		traceMatchesToDisable = new IdentityHashSet();
		removeNonMatchingSymbols();
		pruneTagsAndDisableShadowsForRemovedTracematches();
		traceMatchesToDisable = null;

		AbcTimer.mark("TMAnalysis: remove non-matching symbols");
	}
	
	/**
	 * For all tracematches, checks if all their symbols actually applied anywhere.
	 * If not, edges with that symbol are removed and if by doing so the final states
	 * become unreachable, we remove the tracematch entirely.
	 */
	protected void removeNonMatchingSymbols() {	
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();

		//make a copy of this list, because we alter it while iterating over it
		List traceMatches = new ArrayList(gai.getTraceMatches());
		//for all tracematches
		for (Iterator iter = traceMatches.iterator(); iter.hasNext();) {
			TraceMatch tm = (TraceMatch) iter.next();

			//if the tracematch has no matching shadows at all
			//or after removing the ones which don't match, we get an empty automaton...
			if(!ShadowRegistry.v().hasMatchingShadows(tm)  || tm.quickCheck()) {
				//mark this tm as to be disabled
				traceMatchesToDisable.add(tm);
			}
		}
	}
	
	/**
	 * For all tracematches to remove, remove all information related to those tracematches from the
	 * {@link MatchingTMSymbolTag}s. Delete a tag entirely if it becomes empty.
	 * Also, remove all shadows of a disabled tracematch.
	 * The {@link ShadowRegistry} takes care of all of this, including removing the
	 * tracematch itself if there are no shadows left for it.
	 */
	protected void pruneTagsAndDisableShadowsForRemovedTracematches() {
		if(!traceMatchesToDisable.isEmpty()) {
			
			for (Iterator tmIter = traceMatchesToDisable.iterator(); tmIter.hasNext();) {
				TraceMatch tm = (TraceMatch) tmIter.next();
				
				//get all shadows for this tracematch
				Set shadowsForDisabledTm = ShadowRegistry.v().allShadowIDsForTraceMatch(tm.getName());
				//disable them all
				disableAll(shadowsForDisabledTm);
			}
		}
	}
	
	//singleton pattern

	protected static QuickCheck instance;

	private QuickCheck() {}
	
	public static QuickCheck v() {
		if(instance==null) {
			instance = new QuickCheck();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}

}

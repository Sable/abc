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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;
import abc.tm.weaving.weaver.tmanalysis.util.Statistics;
import abc.tm.weaving.weaver.tmanalysis.util.Timer;

/**
 * An abstract analysis {@link Stage} that provides base functionality such as updating shadows and running assertion checks.
 * Shadows can be updated in two different flavours:
 * <ul>
 *   <li> They can be disabled using {@link #disableShadow(String)} or {@link #disableAll(Set)}, which means that they are
 *        deregistered with {@link ShadowRegistry}, they are pruned from existing {@link MatchingTMSymbolTag}s,
 *        those tags are removed if empty and finally the entire tracematch(es) those shadows belong to are removed
 *        from the {@link TMGlobalAspectInfo} if there are no shadows remaining for them. This functionality
 *        if mostly implemented in {@link ShadowRegistry#disableShadow(String)} and
 *        {@link ShadowRegistry#removeTracematchesWithNoRemainingShadows()}.
 *   <li> They can be explicitly retained using {@link #retainShadow(String)} or {@link #retainAll(Set)},
 *        meaning that no later stage can remove those shadows. This should be called for shadows that become known
 *        to be crucial for a sound weaving process. 
 * </ul>
 *
 * @author Eric Bodden
 */
public abstract class AbstractAnalysisStage implements Stage {

	/** set of shadows to disable after this stage has completed */
	private Set shadowsToDisable;
	
	/** set of shadows to explicitly retain after this stage has completed */
	private Set shadowsToRetain;
	
	/** set of validation checks to run after this stage has completed and the shadows have been updated */
	protected Set validationChecks;
	
	/** name of this analysis stage; inferred from class name; used for statistical output */
	protected final String name;
	
	/** timer to time the duration of this stage */
	protected final Timer stageTimer;
	
	/** timer to time the duration of shadow updating */
	protected final Timer shadowUpdateTimer;

	/**
	 * Creates a new analysis stage without any assertion checks.
	 */
	protected AbstractAnalysisStage() {
		validationChecks = Collections.EMPTY_SET;
		String name = getClass().getName();
		if(name.indexOf('.')>-1) name = name.substring(name.lastIndexOf('.')+1);
		this.name = name;
		this.stageTimer = new Timer(name+"-stage");
		this.shadowUpdateTimer = new Timer(name+"-shadow-update-time");
	}
	
	/**
	 * Creates a new analysis stage with an attached assertion check.
     * @param validationCheck a check in form of a {@link Runnable}; it should throw an exception
     * if the check is violated
	 */
	protected AbstractAnalysisStage(Runnable validationCheck) {
		this();
		validationChecks = new HashSet();
		validationChecks.add(validationCheck);
	}
	
	/**
	 * Creates a new analysis stage with an attached assertion checks.
     * @param validationChecks a set of validation checks
     * @see #AbstractAnalysisStage(Runnable)
	 */
	protected AbstractAnalysisStage(Set validationChecks) {
		this();
		this.validationChecks = new HashSet(validationChecks);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void apply() {
		shadowsToDisable = new HashSet();
		shadowsToRetain = new HashSet();
		stageTimer.startOrResume();
		doAnalysis();
		stageTimer.stop();
		shadowUpdateTimer.startOrResume();		
		updateShadows();
		shadowUpdateTimer.stop();
		defaultStatistics();
		appendStatistics();
		runChecks();
		Statistics.lastStageCompleted = name;
		shadowsToDisable = null;
		shadowsToRetain = null;
	}
	
	/**
	 * Performs the actual analysis.
	 */
	protected abstract void doAnalysis();
	
	/**
	 * Runs all attached checks.
	 */
	protected void runChecks() {
		for (Iterator checkIter = validationChecks.iterator(); checkIter.hasNext();) {
			Runnable check = (Runnable) checkIter.next();
			check.run();
		}
		
	}

	/**
	 * Adds the given shadows to the set of shadows to disable.
	 * @param shadowsToDisable a set of unique shadow IDs
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	protected void disableAll(Set shadowsToDisable) {
		for (Iterator iterator = shadowsToDisable.iterator(); iterator.hasNext();) {
			String uniqueShadowId = (String) iterator.next();
			disableShadow(uniqueShadowId);
		}
	}
	
	/**
	 * Adds the given shadow to the set of shadows to disable.
	 * @param uniqueShadowId ID of the shadow to disable
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	protected void disableShadow(String uniqueShadowId) {
		assert !shadowsToRetain.contains(uniqueShadowId);
		shadowsToDisable.add(uniqueShadowId);
	}

	/**
	 * Adds the given shadows to the set of shadows to explicitly retain.
	 * @param shadowsToDisable a set of unique shadow IDs
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	protected void retainAll(Set shadowsToRetain) {
		for (Iterator iterator = shadowsToRetain.iterator(); iterator.hasNext();) {
			String uniqueShadowId = (String) iterator.next();
			retainShadow(uniqueShadowId);
		}
	}
	
	/**
	 * Adds the given shadow to the set of shadows to retain.
	 * @param uniqueShadowId ID of the shadow to retain
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	protected void retainShadow(String uniqueShadowId) {
		assert !shadowsToDisable.contains(uniqueShadowId);
		shadowsToRetain.add(uniqueShadowId);
	}
	
	/**
	 * Logs the given label/value pair to the statistics.
	 * @param label any label
	 * @param value any object
	 */
	protected void logToStatistics(String label, Object value) {
		Statistics.print(name+"-"+label, value);
	}
	
	/**
	 * Logs the given label/value pair to the statistics.
	 * @param label any label
	 * @param value any int
	 */
	protected void logToStatistics(String label, int value) {
		logToStatistics(label, value+"");
	}

	/**
	 * Processes all shadows that were registered as  to be retained or disabled, i.e.
	 * really marks them as to be retained or disables them in the {@link ShadowRegistry}.
	 */
	 private void updateShadows() {
		for (Iterator retainIter = shadowsToRetain.iterator(); retainIter.hasNext();) {
			String uniqueShadowId = (String) retainIter.next();
			ShadowRegistry.v().retainShadow(uniqueShadowId);
		}
		
		for (Iterator disableIter = shadowsToDisable.iterator(); disableIter.hasNext();) {
			String uniqueShadowId = (String) disableIter.next();
			ShadowRegistry.v().disableShadow(uniqueShadowId);
		}
		
		//if there were actually shadows disabled, ask the ShadowRegistry to
		//see if any tracematches can be removed
		if(!shadowsToDisable.isEmpty()) {
			ShadowRegistry.v().removeTracematchesWithNoRemainingShadows();
		}
	}
	 
	/**
	 * Subclasses can overwrite this method to add something to the statistics.
	 * Use {@link #logToStatistics(String, Object)} to do so.
	 */
	protected void appendStatistics() {
	}

	/**
	 * Outputs some statistics such as analysis time and number of removed and remaining shadows.
	 */
	protected void defaultStatistics() {
		int numRemovedShadows = shadowsToDisable.size();
		int numRetainedShadows = shadowsToRetain.size();
		int numRemainingShadows = ShadowRegistry.v().enabledShadows().size();
			
		logToStatistics("shadows-removed", numRemovedShadows+"");
		logToStatistics("shadows-retained", numRetainedShadows+"");
		logToStatistics("shadows-remaining", numRemainingShadows+"");
		logToStatistics("stage-time", stageTimer);
		logToStatistics("shadow-update-time", shadowUpdateTimer);
	}

}

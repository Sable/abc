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
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import abc.main.Debug;

/**
 * A global registry for shadow groups.
 *
 * @author Eric Bodden
 */
public class ShadowGroupRegistry {
	
	protected Set<ShadowGroup> shadowGroups;
	
	/**
	 * Registers a collection of {@link ShadowGroup}s with this registry.
	 * @param groups
	 */
	public void registerShadowGroups(Collection<ShadowGroup> groups) {
		if(shadowGroups==null) {
			shadowGroups = new HashSet<ShadowGroup>();
		}		
		shadowGroups.addAll(groups);
	}
	
	/**
	 * Returns all shadow groups currently registered.
	 * @return a set of all {@link ShadowGroup}s currently registered
	 */
	public Set<ShadowGroup> getAllShadowGroups() {
		if(shadowGroups==null) {
			throw new RuntimeException("Shadow groups not yet available. Apply FlowInsensitiveAnalysis first.");
		}		
		return Collections.unmodifiableSet(shadowGroups);
	}
	
	/**
	 * Prunes all shadow groups which have a label-shadow equal to one one of the shadows in deadShadows.
	 * Disables all shadows in deadShadows.
	 * Then disables all (other) shadows which are not member of a shadow group any more.
	 * @param deadShadows a set of shadows which is known to be dead, i.e. unnecessary
	 * @return <code>true</code> if a shadow could actually be disabled
	 * @see ShadowGroup#getLabelShadows()
	 * @see {@link ShadowRegistry#disableShadow(String)}
	 */
	public boolean pruneShadowsAndIncompleteDependentGroups(Collection deadShadows) {
		boolean removedAGroup = true;
		Set allShadowsInAllShadowGroups = new HashSet();
		
		//disable all dead shadows
		for (Iterator deadShadowIter = deadShadows.iterator(); deadShadowIter.hasNext();) {
			SymbolShadowWithPTS shadow = (SymbolShadowWithPTS) deadShadowIter.next();
			if(Debug.v().tmShadowDump) {
				System.err.println("Disabling shadow "+shadow.getUniqueShadowId()+
						" because it was marked as dead.");
			}
			ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
		}
		
		//prune all groups which have a dead shadow as a label-shadow
		for (Iterator groupIter = shadowGroups.iterator(); groupIter.hasNext();) {
			ShadowGroup group = (ShadowGroup) groupIter.next();

			//collect all shadows
			allShadowsInAllShadowGroups.addAll(group.getAllShadows());
			
			Set labelShadows = group.getLabelShadows();
			for (Iterator shadowIter = deadShadows.iterator(); shadowIter.hasNext();) {
				SymbolShadowWithPTS deadShadow = (SymbolShadowWithPTS) shadowIter.next();
				if(labelShadows.contains(deadShadow)) {
					if(Debug.v().debugTmAnalysis) {
						System.err.println("Removed shadow group #"+group.getNumber()+
								" because it contains label-shadow "+deadShadow.getUniqueShadowId()+
								", which is dead.");
					}
					groupIter.remove();
					removedAGroup = true;
					break;
				}
			}
		}
		
		if(removedAGroup) {
			//collect all shadows which are still active, i.e. still contained in a remaining shadow group
			Set allShadowsStillActive = new HashSet();
			for (Iterator<ShadowGroup> groupIter = shadowGroups.iterator(); groupIter.hasNext();) {
				ShadowGroup group = groupIter.next();
				allShadowsStillActive.addAll(group.getAllShadows());
			}
			
			//we can disable all shadows which were in a shadow group before but now are not any more
			Set shadowsToDisable = new HashSet(allShadowsInAllShadowGroups);
			shadowsToDisable.removeAll(allShadowsStillActive);			
			for (Iterator shadowIter = shadowsToDisable.iterator(); shadowIter.hasNext();) {
				SymbolShadowWithPTS shadow = (SymbolShadowWithPTS) shadowIter.next();
				ShadowRegistry.v().disableShadow(shadow.getUniqueShadowId());
				if(Debug.v().tmShadowDump) {
					System.err.println("Removed shadow "+shadow.getUniqueShadowId()+
							" because it is no longer part of any active shadow group.");
				}
			}			
			return !shadowsToDisable.isEmpty();
		} else {
			return false;
		}
	}

	//singleton pattern
	
	protected static ShadowGroupRegistry instance;
	
	private ShadowGroupRegistry() {}
	
	public static ShadowGroupRegistry v() {
		if(instance==null) {
			instance = new ShadowGroupRegistry();
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

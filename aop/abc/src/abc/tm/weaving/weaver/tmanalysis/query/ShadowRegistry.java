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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import soot.SootMethod;
import soot.tagkit.Host;
import soot.tagkit.SourceLnNamePosTag;
import soot.tagkit.SourceLnPosTag;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.AdviceApplicationVisitor;
import abc.weaving.weaver.AdviceApplicationVisitor.AdviceApplicationHandler;

/**
 * A global registry of all shadows in the program.
 * The singleton object gives access to all those shadows and allows to disable them.
 *
 * @author Eric Bodden
 */
public class ShadowRegistry {
	
    protected TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
	
	protected Map<String,AdviceApplication> allShadowsToSymbolAdviceApplications;
	
    protected Map<String,AdviceApplication> allShadowsToSyncAdviceApplications;

    protected Map<String,AdviceApplication> allShadowsToSomeAdviceApplications;
    
    protected Map<String,AdviceApplication> allShadowsToBodyAdviceApplications;

    protected Map<String,Set<String>> tmNameToUniqueShadowIds;
	
	protected Set<String> enabledShadows;  
	
	protected Set<String> disabledShadows;  
	
	protected Set<String> shadowsToBeRetained;
	
	/** maps a shadow ID to a unique number for that id */
	protected Map<String,Integer> shadowIdToNumber = new HashMap<String,Integer>();
	
    protected boolean residueBoxesChanged;

    protected ShadowRegistry() {
	
		allShadowsToSymbolAdviceApplications= new HashMap();
        allShadowsToSyncAdviceApplications= new HashMap();
        allShadowsToSomeAdviceApplications= new HashMap();
        allShadowsToBodyAdviceApplications= new HashMap();
		tmNameToUniqueShadowIds = new HashMap();
		residueBoxesChanged = false;

		//traverse all advice applications
		AdviceApplicationVisitor.v().traverse(
				new AdviceApplicationHandler() {

					public void adviceApplication(AdviceApplication aa,SootMethod context) {

						//if we have a tracematch advice
						if(aa.advice instanceof PerSymbolTMAdviceDecl) {
							
							//count the shadow
							PerSymbolTMAdviceDecl decl = (PerSymbolTMAdviceDecl) aa.advice;

							//get the tracematch for that advice application
							String traceMatchID = decl.getTraceMatchID();

							String qualifiedShadowId = Naming.uniqueShadowID(traceMatchID, decl.getSymbolId(),aa.shadowmatch.shadowId).intern();
							Object old = allShadowsToSymbolAdviceApplications.put(qualifiedShadowId, aa);
							assert old==null; //IDs should be unique
							
							Set shadowIds = tmNameToUniqueShadowIds.get(traceMatchID);
							if(shadowIds==null) {
								shadowIds = new HashSet();
								tmNameToUniqueShadowIds.put(traceMatchID,shadowIds);
							}
							boolean added = shadowIds.add(qualifiedShadowId);
							assert added; //IDs should be unique
						} else if(aa.advice instanceof TMAdviceDecl) {
                            TMAdviceDecl decl = (TMAdviceDecl) aa.advice;
                            if(decl.isSynch()) {
                                //get the tracematch for that advice application
                                String traceMatchID = decl.getTraceMatchID();
                                String locationShadowId = Naming.locationID(traceMatchID, aa.shadowmatch.shadowId).intern();
                                allShadowsToSyncAdviceApplications.put(locationShadowId, aa);
                            } else if(decl.isSome()) {
                                //get the tracematch for that advice application
                                String traceMatchID = decl.getTraceMatchID();
                                String locationShadowId = Naming.locationID(traceMatchID, aa.shadowmatch.shadowId).intern();
                                allShadowsToSomeAdviceApplications.put(locationShadowId, aa);
                            } else if(decl.isBody()) {
                                //get the tracematch for that advice application
                                String traceMatchID = decl.getTraceMatchID();
                                String locationShadowId = Naming.locationID(traceMatchID, aa.shadowmatch.shadowId).intern();
                                allShadowsToBodyAdviceApplications.put(locationShadowId, aa);
                            }
                        }
					} 
				}
		);
		
		//right now, all shadows are enabled, none are disabled, none to be retained
		enabledShadows = new HashSet(allShadowsToSymbolAdviceApplications.keySet());
		disabledShadows = new HashSet();
		shadowsToBeRetained = new HashSet();
		
		//instantly remove tracematches from the system which have no matches at all
		removeTracematchesWithNoRemainingShadows();
	}
	
	public Set traceMatchesWithMatchingShadows() {
		return new HashSet(tmNameToUniqueShadowIds.keySet());
	}
	
	public boolean hasMatchingShadows(TraceMatch tm) {
		return traceMatchesWithMatchingShadows().contains(tm.getName());
	}
	
	public Set allShadowIDsForTraceMatch(String traceMatchName) {
		if(!tmNameToUniqueShadowIds.containsKey(traceMatchName)) {
			return Collections.EMPTY_SET;
		} else {
			return new HashSet((Set) tmNameToUniqueShadowIds.get(traceMatchName));
		}
	}
		
	public void disableShadow(String uniqueShadowId) {
		
		//remove the tag from this registry
		assert !shadowsToBeRetained.contains(uniqueShadowId);
		//modify residue
		conjoinShadowWithResidue(uniqueShadowId, NeverMatch.v());
		
		boolean removed = enabledShadows.remove(uniqueShadowId);
		assert removed;
		boolean added = disabledShadows.add(uniqueShadowId);
        assert added;
		
		if(Debug.v().tmShadowDump) {
			System.err.println("disabled shadow: "+uniqueShadowId);
		}

	}

	/**
	 * Conjoins the residue for the given shadow with the given conjunct.
	 * @param uniqueShadowId a valid ID of a given shadow
	 * @param conjunct the residue to conjoin with
	 */
	public void conjoinShadowWithResidue(String uniqueShadowId, Residue conjunct) {
		assert allShadowsToSymbolAdviceApplications.containsKey(uniqueShadowId);
		AdviceApplication aa = (AdviceApplication) allShadowsToSymbolAdviceApplications.get(uniqueShadowId);		
		aa.setResidue(AndResidue.construct(aa.getResidue(),conjunct));
		residueBoxesChanged = true;

		//print a warning message (usually for test harness)
		if(Debug.v().warnWhenAlteringShadow)
			printWarning(aa,uniqueShadowId,conjunct);
	}
	
    /**
     * Tells whether any residue was set since the last time this method was called
     * (or since startup of the program).
     * @return <code>true</code> is any residue box was changed since the last
     * call to this method or program startup
     */
    public boolean wasAnyResidueChanged() {
        boolean val = residueBoxesChanged;
        residueBoxesChanged = false;
        return val;
    }

	public void removeTracematchesWithNoRemainingShadows() {
		for (Iterator tmIter = gai.getTraceMatches().iterator(); tmIter.hasNext();) {
			TraceMatch tm = (TraceMatch) tmIter.next();
			
			//get all the enabled shadows for this TM
			Set thisTMsShadowIDs = new HashSet(allShadowIDsForTraceMatch(tm.getName()));
			thisTMsShadowIDs.retainAll(enabledShadows());
			
			if(thisTMsShadowIDs.isEmpty()) {
				boolean removed = gai.removeTraceMatch(tm);
				assert removed;
				tmNameToUniqueShadowIds.remove(tm.getName());
			}
		}
	}
	
	public void retainShadow(String uniqueShadowId) {
		shadowsToBeRetained.add(uniqueShadowId);
		assert !disabledShadows.contains(uniqueShadowId);
	}
	
	public void disableAllInactiveShadows() {
		Set copyEnabled = new HashSet(enabledShadows); 
		for (Iterator enabledIter = copyEnabled.iterator(); enabledIter.hasNext();) {
			String uniqueShadowId = (String) enabledIter.next();
			if(!shadowsToBeRetained.contains(uniqueShadowId)) {
				disableShadow(uniqueShadowId);
			}
		}
		
		//validation code
		assert sanityCheck();
	}
	
	public Set enabledShadows() {
		return new HashSet(enabledShadows);
	}
	
	public boolean enabledShadowsLeft() {
		return !enabledShadows.isEmpty();
	}

	public Set allShadows() {
		return new HashSet(allShadowsToSymbolAdviceApplications.keySet());
	}
	
	public AdviceApplication getSymbolAdviceApplicationForShadow(String uniqueShadowId) {
		return (AdviceApplication) allShadowsToSymbolAdviceApplications.get(uniqueShadowId);
	}
	
	public void dumpShadows() {
		if(Debug.v().tmShadowDump) {
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			System.err.println("DISABLED SHADOWS");
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			for (Iterator entryIter = allShadowsToSymbolAdviceApplications.entrySet().iterator(); entryIter.hasNext();) {
				Entry entry = (Entry) entryIter.next();
				String shadowId = (String) entry.getKey();
				if(!enabledShadows.contains(shadowId)) {
					AdviceApplication aa = (AdviceApplication) entry.getValue();
					StringBuffer sb = new StringBuffer();
					aa.debugInfo("  ", sb);
					System.err.println("unique-shadow-id: "+shadowId);
					System.err.println("status: disabled");
					System.err.println("applied in method: "+aa.shadowmatch.getContainer());					
					System.err.println(sb.toString());
					System.err.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
				}
			}
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			System.err.println("REMAINING SHADOWS");
			System.err.println("===============================================================================");
			System.err.println("===============================================================================");
			for (Iterator entryIter = allShadowsToSymbolAdviceApplications.entrySet().iterator(); entryIter.hasNext();) {
				Entry entry = (Entry) entryIter.next();
				String shadowId = (String) entry.getKey();
				
				if(enabledShadows.contains(shadowId)) {
					AdviceApplication aa = (AdviceApplication) entry.getValue();
					StringBuffer sb = new StringBuffer();
					aa.debugInfo("  ", sb);
					System.err.println("unique-shadow-id: "+shadowId);
					System.err.println("status: enabled");
					System.err.println("applied in method: "+aa.shadowmatch.getContainer());					
					System.err.println(sb.toString());
					System.err.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
				}
			}
		}
	}
	
	
	private boolean sanityCheck() {
		assert enabledShadows.containsAll(shadowsToBeRetained);
		
		Set enabled = new HashSet(enabledShadows);
		assert !enabled.removeAll(disabledShadows);
		
		Set all = new HashSet();
		all.addAll(enabledShadows);
		all.addAll(disabledShadows);
		assert all.equals(allShadowsToSymbolAdviceApplications.keySet());
		return true;
	}

	/**
     * Returns <code>true</code> if the shadow with this unique id is still enabled.
	 */
	public boolean isEnabled(String uniqueShadowId) {
		assert allShadowsToSymbolAdviceApplications.keySet().contains(uniqueShadowId);
		return enabledShadows.contains(uniqueShadowId);
	}

	public Set allActiveShadowsForTag(SymbolShadowTag tag, SootMethod container) {
		Set result = new HashSet();

		for (ISymbolShadow match : tag.getAllMatches()) {
			if(match.isEnabled()) {
				result.add(new SymbolShadowWithPTS(match,container));
			}
		}
		return result;
	}
	
	public Set<SymbolShadowWithPTS> allActiveShadowsForHost(Host h, SootMethod container) {
		if(h.hasTag(SymbolShadowTag.NAME)) {
			SymbolShadowTag tag = (SymbolShadowTag) h.getTag(SymbolShadowTag.NAME);
			return allActiveShadowsForTag(tag, container);
		} else {
			return Collections.EMPTY_SET; 
		}
	}

	public Set allActiveShadowsForHostAndTM(Host h, SootMethod container, TraceMatch tm) {
		Set result = new HashSet();
		Set allShadowsForHost = allActiveShadowsForHost(h, container);
		for (Iterator shadowIter = allShadowsForHost.iterator(); shadowIter.hasNext();) {
			SymbolShadowWithPTS shadow = (SymbolShadowWithPTS) shadowIter.next();
			String shadowId = shadow.getUniqueShadowId();
			String tracematchName = Naming.getTracematchName(shadowId);
			if(tracematchName.equals(tm.getName())) {
				result.add(shadow);
			}
		}
		return result;
	}
    
    public AdviceApplication getSomeAdviceApplicationForSymbolShadow(String uniqueShadowId) {
        AdviceApplication aa = allShadowsToSomeAdviceApplications.get(Naming.locationID(uniqueShadowId));
        assert aa!=null && ((TMAdviceDecl)aa.advice).isSome();
        return aa;
    }
    
    public AdviceApplication getSyncAdviceApplicationForSymbolShadow(String uniqueShadowId) {
        AdviceApplication aa = allShadowsToSyncAdviceApplications.get(Naming.locationID(uniqueShadowId));
        assert aa!=null && ((TMAdviceDecl)aa.advice).isSynch();
        return aa;
    }
	
    /**
     * @param uniqueShadowId
     * @return may return <code>null</code>
     */
    public AdviceApplication getBodyAdviceApplicationForSymbolShadow(String uniqueShadowId) {
        AdviceApplication aa = allShadowsToBodyAdviceApplications.get(Naming.locationID(uniqueShadowId));
        assert aa==null || ((TMAdviceDecl)aa.advice).isBody();
        return aa;
    }

    protected void disableSyncSomeAndBodyAdviceIfNotNeededAnyMore(String uniqueShadowIdOrLocationID) {
        String locationID = Naming.locationID(uniqueShadowIdOrLocationID);
        for (String enabledShadow : enabledShadows) {
            if(Naming.locationID(enabledShadow).equals(locationID)) {
                AdviceApplication symAa = allShadowsToSymbolAdviceApplications.get(enabledShadow);
                //the if-check is for the following reason:
                //if a shadow was moved, it still shows up as "enabled" not to confuse subsequent steps;
                //however, its residue is nevermatch and hence, we can safely disable all surrounding
                //helper advice
                //FIXME do this properly...
                if(!NeverMatch.neverMatches(symAa.getResidue())) {
                    //is not yet disabled
                    return;
                }
            }
        }
        //no enabled symbol shadows found for this location
        AdviceApplication someAa = getSomeAdviceApplicationForSymbolShadow(uniqueShadowIdOrLocationID);
        AdviceApplication syncAa = getSyncAdviceApplicationForSymbolShadow(uniqueShadowIdOrLocationID);
        AdviceApplication bodyAa = getBodyAdviceApplicationForSymbolShadow(uniqueShadowIdOrLocationID);
        //if not already all disabled
        if(!NeverMatch.neverMatches(someAa.getResidue()) || !NeverMatch.neverMatches(syncAa.getResidue()) ||
           (bodyAa!=null && !NeverMatch.neverMatches(bodyAa.getResidue()))) {
            //disable
            System.err.print("No active symbol advice for location "+locationID+". ");
            System.err.println("Disabling some-advice, sync-advice and body advice.");
            someAa.setResidue(NeverMatch.v());
            syncAa.setResidue(NeverMatch.v());
            if(bodyAa!=null)//does not always have to be present, only if a symbol leads to a final state
                bodyAa.setResidue(NeverMatch.v());
        }
        
    }
    
    public void disableAllUnneededSomeSyncAndBodyAdvice() {
        //build a set of all location IDs
        Set<String> locationIDs = new HashSet<String>();
        locationIDs.addAll(allShadowsToSomeAdviceApplications.keySet());
        locationIDs.addAll(allShadowsToSyncAdviceApplications.keySet());
        locationIDs.addAll(allShadowsToBodyAdviceApplications.keySet());
        
        //disable all, if they can be disabled
        for (String inactiveLocationID : locationIDs) {
            disableSyncSomeAndBodyAdviceIfNotNeededAnyMore(inactiveLocationID);
        }
    }

    /**
	 * Prints a warning that the given advice application was changed by the analysis.
	 * @param aa any {@link AdviceApplication}
	 * @param uniqueShadowId the unique shadow ID of the disabled shadow
	 * @param conjunct the residue conjunct that was set
	 */
	private void printWarning(AdviceApplication aa, String uniqueShadowId, Residue conjunct) {
		Position pos = null;
		Host host = aa.shadowmatch.getHost();
		if(host.hasTag("SourceLnPosTag")) {
			SourceLnPosTag tag = (SourceLnPosTag) host.getTag("SourceLnPosTag");
			String fileName = "";
			if(tag instanceof SourceLnNamePosTag) {
				SourceLnNamePosTag sourceLnNamePosTag = (SourceLnNamePosTag) tag;
				fileName = sourceLnNamePosTag.getFileName();
			}			
			pos = new Position(fileName,tag.startLn(),tag.startPos(),tag.endLn(),tag.endPos());
		}
		Main.v().getAbcExtension().forceReportError(
				ErrorInfo.WARNING,
				"tracematch shadow "+uniqueShadowId+" changed at this position (conjoined with "+conjunct+")",
				pos
		);
	}
	
	/**
	 * Returns a unique int number for the given shadow id.
	 * The numbers start with 0 and then increase by 1.
	 * @param uniqueShadowId
	 * @return
	 */
	public int numberOf(String uniqueShadowId) {
		Integer number = (Integer) shadowIdToNumber.get(uniqueShadowId);
		if(number==null) {
			number = new Integer(shadowIdToNumber.size());
			shadowIdToNumber.put(uniqueShadowId, number);
		}
		if(Debug.v().debugTmAnalysis) {
		    System.err.println("number of "+uniqueShadowId+": "+number);
		}
		return number;
	}

	//singleton pattern
	
	public static void initialize() {
		v();
	}

	protected static ShadowRegistry instance;
	
	public static ShadowRegistry v() {
		if(instance==null) {
			instance = new ShadowRegistry();
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

/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
 * Copyright (C) 2008 Patrick Lam
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
package abc.tmwpopt.fsanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tmwpopt.fsanalysis.Ranking.PotentialFailureGroup;
import abc.tmwpopt.fsanalysis.util.ShadowsPerTMSplitter;


/**
 * This class dumps statistics in various formats to files. It can create PFG files
 * which contain potential points of failure in raw text, and it can output a combination
 * of HTML and DOT files fr visualization purposes.
 * 
 * @see Debug#outputPFGs
 * @see Debug#outputHTML
 */
public class Statistics {
	
    public static final String urlPrefix = 
		"http://anjou.uwaterloo.ca/cgi-bin/view.cgi?name=";
    
	boolean removeIfExists = true;
  
	/**
	 * Dumps remaining shadows to PFG and/or HTML files.
	 * @param message a prefix to be printed
	 * @param inShadows the set of remaining shadows
	 * @param numberOnly if <code>true</code>, only the number of shadows will be printed to the PFG files,
	 * if <code>false</code>, the potential points of failure will be printed themselves, and HTML is generated
	 * if enabled via {@link Debug#outputHTML}.
	 * 
	 * If {@link Debug#outputPFGs} is <code>false</code>, nothing is written to PFG files.
	 */
	public void dump(String message, Set<Shadow> inShadows, boolean numberOnly) {
		Set<Shadow> shadows = new HashSet<Shadow>(inShadows);
		
		//remove all non-symbol shadows and all disabled shadows
		for (Iterator<Shadow> iter = shadows.iterator(); iter.hasNext();) {
			Shadow s = iter.next();
			if(!s.isEnabled() || !(s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl)) {
				iter.remove();
			}
		}
		
		//for each tracematch
		Map<TraceMatch, Set<Shadow>> splitSymbolShadows = ShadowsPerTMSplitter.splitSymbolShadows(shadows);		
		for (Map.Entry<TraceMatch, Set<Shadow>> entry : splitSymbolShadows.entrySet()) {
			TraceMatch tm = entry.getKey();
			Set<Shadow> perTMShadows = entry.getValue();

			//compute symbol names
	        Map<SootMethod,String> adviceMethodToSymbol = new HashMap<SootMethod, String>();
	        for (String sym : tm.getSymbols()) {
				SootMethod adviceMethod = tm.getSymbolAdviceMethod(sym);
				adviceMethodToSymbol.put(adviceMethod,sym);
			}
	        Map<Shadow,String> shadowToSymbolName = new HashMap<Shadow, String>();
	        for (Shadow shadow : perTMShadows) {
				SootMethod adviceMethod = shadow.getAdviceDecl().getImpl().getSootMethod();
				String symbol = adviceMethodToSymbol.get(adviceMethod);
				assert symbol!=null;
				shadowToSymbolName.put(shadow, symbol);
			}
			
	        //remove artificial shadows
			for (Iterator<Shadow> iter = perTMShadows.iterator(); iter.hasNext();) {
				Shadow s = iter.next();
				if(shadowToSymbolName.get(s).equals("newDaCapoRun")) {
					iter.remove();
				}
			}
			
			//build qualified tracematch name
			String tmName = tm.getContainerClass().getShortName();
			tmName += "." + tm.getName();

			String bmName = Scene.v().getMainClass().getName();
			String mainClassShortName = bmName;
			// Evil hack!
			if (bmName.startsWith("dacapo."))
			    mainClassShortName = bmName.split("\\.")[1];

			String fileName = bmName+"-"+tmName+".pfg";
			String HTMLfileName = bmName+"-"+tmName+".in.html";
			if(removeIfExists) {
				new File(fileName).delete();
			}
			// we really don't want three copies of the HTML output.
			new File(HTMLfileName).delete();
			
			try {
				Set<Shadow> ppfs = filterPotentialPointOfFailures(perTMShadows);
				List<PotentialFailureGroup> sortedGroups = Ranking.v().rankAndSort(ppfs, tm);

				if(Debug.v().outputPFGs) {
					PrintWriter out = new PrintWriter(new FileOutputStream(fileName,true));
					out.println("Number of potential failure groups "+message+": "+sortedGroups.size());
					if(!numberOnly) {
						for (PotentialFailureGroup pfg : sortedGroups) {
							out.println(pfg);
							out.println();
							out.println();
						}
					}
					out.close();
				}
				
				if(!numberOnly && Debug.v().outputHTML){
					PrintWriter hOut = new PrintWriter(new FileOutputStream(HTMLfileName,true));
					int c = 1;
					for (PotentialFailureGroup pfg : sortedGroups) {
						hOut.print(pfg.ppfAndGroupToHTMLString(urlPrefix+mainClassShortName+"/", c++));
					}
					hOut.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if(!numberOnly && Debug.v().outputHTML)
				OutputDotGraphs.v().apply(tm);
		}
		removeIfExists = false;
	}	

	/**
	 * This methods takes a set of shadows as argument and returns a fresh set
	 * containing all shadows in <i>shadows</i> that are potential points of failure,
	 * i.e. are final shadows, i.e. shadows of a final symbol that may drive
	 * the tracematch into a final state.
	 */
	private Set<Shadow> filterPotentialPointOfFailures(Set<Shadow> shadows) {
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		Set<Shadow> PPFs = new HashSet<Shadow>();
		
    	Map<SootMethod,String> adviceMethodToSymbol = new HashMap<SootMethod, String>();
    	Map<SootMethod,TraceMatch> adviceMethodToTraceMatch = new HashMap<SootMethod, TraceMatch>();
    	for(TraceMatch tm : gai.getTraceMatches()) {	        
	        for (String sym : tm.getSymbols()) {
				SootMethod adviceMethod = tm.getSymbolAdviceMethod(sym);
				adviceMethodToSymbol.put(adviceMethod,sym);
				adviceMethodToTraceMatch.put(adviceMethod,tm);
			}
    	}
        Set<Shadow> allShadows = new HashSet<Shadow>(shadows);
        for (Shadow s : allShadows) {
			if(s.isEnabled() && (s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl)) {
				SootMethod adviceMethod = s.getAdviceDecl().getImpl().getSootMethod();
				String symbol = adviceMethodToSymbol.get(adviceMethod);
				assert symbol!=null;
				if(adviceMethodToTraceMatch.get(adviceMethod).getFinalSymbols().
						contains(adviceMethodToSymbol.get(adviceMethod))) {
					PPFs.add(s);
				}
			}
		}
                
        return PPFs;
	}

	//singleton pattern
	
	protected static Statistics instance;

    private Statistics() { }

    public static Statistics v() {
		if(instance==null) {
			instance = new Statistics();
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

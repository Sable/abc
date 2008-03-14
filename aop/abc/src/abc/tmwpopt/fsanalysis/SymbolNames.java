/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

import java.util.HashMap;
import java.util.Map;

import soot.SootMethod;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;


/**
 * This class keeps track of the tracematch symbol name associated with a shadow.
 * @author Eric Bodden
 */
public class SymbolNames {

	protected HashMap<Shadow, String> shadowToSymbolName;
	protected HashMap<SootMethod, TraceMatch> adviceMethodToTraceMatch;
	
    private SymbolNames() { 
    	shadowToSymbolName = new HashMap<Shadow, String>();
		adviceMethodToTraceMatch = new HashMap<SootMethod, TraceMatch>();
		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		for (TraceMatch tm : gai.getTraceMatches()) {
			for (String tmSymbol : tm.getSymbols()) {
				adviceMethodToTraceMatch.put(tm.getSymbolAdviceMethod(tmSymbol), tm);
			}
		}
    }
    
	/**
	 * Returns the symbol name for the given shadow or <code>null</code> if there is none.
	 */
	public String symbolNameForShadow(Shadow s) {
		if(!(s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl))
			return null;
	
		String symbolName = shadowToSymbolName.get(s);
		if(symbolName==null) {
			//compute symbol name
			SootMethod adviceMethod = s.getAdviceDecl().getImpl().getSootMethod();
			TraceMatch tm = adviceMethodToTraceMatch.get(adviceMethod);
	        Map<SootMethod,String> adviceMethodToSymbol = new HashMap<SootMethod, String>();
	        for (String sym : tm.getSymbols()) {
				SootMethod am = tm.getSymbolAdviceMethod(sym);
				adviceMethodToSymbol.put(am,sym);
			}
	        Map<Shadow,String> shadowToSymbolName = new HashMap<Shadow, String>();
			
	        symbolName = adviceMethodToSymbol.get(adviceMethod);
			shadowToSymbolName.put(s, symbolName);
		}
		
		return symbolName;
	}
	
	/**
	 * Returns <code>true</code>, if the symbol name of the given shadow
	 * is <code>newDaCapoRun</code>.
	 */
	public boolean isArtificialShadow(Shadow s) {
		String symbolName = symbolNameForShadow(s);
		return symbolName!=null && symbolName.equals("newDaCapoRun");
	}
	
	
	//singleton pattern
	
	protected static SymbolNames instance;

    public static SymbolNames v() {
		if(instance==null) {
			instance = new SymbolNames();
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

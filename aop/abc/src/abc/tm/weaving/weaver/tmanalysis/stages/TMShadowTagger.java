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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.LocalSplitter;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.MethodCategory;

/**
 * This class recovers tracematch shadows by applying an intraprocedural flow analysis.
 * A tracematch shadow, where two transitions a and b are executed (with the semantics
 * a <i>or</i> b) has the following form:
 * <ul>
 * <li>sync-shadow
 * <li>symbol-shadow for a
 * <li>symbol-shadow for b
 * <li>some-shadow (see OOPSLA 05 paper)
 * </ul>
 *
 * The analysis recovers that structure and then tags the call statement to the some-shadow
 * with a tag that is labeled {a,b}.
 * <br><br>
 * Assumes that <code>OptionsParser.v().set_tag_instructions(true);</code> has been called prior to weaving.
 * 
 * This is an idempotent operation.
 * @author Eric Bodden
 */
public class TMShadowTagger extends BodyTransformer implements Stage {

    /**
     * If set to <code>true</code>, the tagger will split local variables, resulting in the fact that
     * advice actuals are unique.
     */
    public static boolean UNIQUE_ADVICE_ACTUALS = false;
    
	/** Mapping from a sync-advice method to its tracematch. */
	protected Map<SootMethod,TraceMatch> syncMethodToTraceMatch;
	
	/**
	 * A tag that holds a mapping from tracematches to {@link SymbolShadow}es that
	 * match at the annotated statement for that tracematch.
	 * @author Eric Bodden
	 */
	public static class SymbolShadowTag implements Tag {
		
		public final static String NAME = SymbolShadowTag.class.getName();
        
		private final Map<TraceMatch, Set<ISymbolShadow>> tmToMatches;		

		public SymbolShadowTag(Map<TraceMatch, Set<ISymbolShadow>> matches) {
			this.tmToMatches = matches;
		}

		public String getName() {
			return NAME;
		}

		public byte[] getValue() throws AttributeValueException {
			throw new UnsupportedOperationException();
		}
		
		/**
		 * Returns all matches for the given tracematch.
		 * @param tm any tracematch
		 * @return all matches for the given tracematch or the empty set if there are no matches
		 */
		public Set<ISymbolShadow> getMatchesForTracematch(TraceMatch tm) {
			Set<ISymbolShadow> symbolShadowMatch = tmToMatches.get(tm); 
			if(symbolShadowMatch==null) {
				return Collections.emptySet();
			} else {
				return symbolShadowMatch;
			}
		}
		
		/**
		 * Returns all matches for all tracematches.
		 */
		public Set<ISymbolShadow> getAllMatches() {
			Set<ISymbolShadow> res = new HashSet<ISymbolShadow>();
			for (Set<ISymbolShadow> matches : tmToMatches.values()) {
				res.addAll(matches);
			}
			return res;
		}
		
		
	}
	
	protected void internalTransform(Body b, String phaseName, Map options) {

		//remove tags (if already present)
		for (Host unit : (Collection<Unit>)b.getUnits()) {
			unit.removeTag(SymbolShadowTag.NAME);
		}
		
		//perform analysis
		SymbolFinder symbolFinder = new SymbolFinder(new ExceptionalUnitGraph(b));
		
		//for each invoke-statement that calls a some-advice
		for (Stmt call : symbolFinder.getSomeAdviceMethodCalls()) {
			//get the symbols matching at this call
			Map<TraceMatch,Set<ISymbolShadow>> matches = symbolFinder.getSymbolsAtSomeAdviceMethodCall(call);
			//create the annotation
			call.addTag(new SymbolShadowTag(matches));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void apply() {
		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//construct mapping
		syncMethodToTraceMatch = new HashMap<SootMethod,TraceMatch>();		
		for (TraceMatch tm : (Collection<TraceMatch>) gai.getTraceMatches()) {
			SootMethod syncMethod = tm.getSynchAdviceMethod();
			syncMethodToTraceMatch.put(syncMethod, tm);
		}		
		
		//transform all weaveable methods in all weavable classes 
		for (AbcClass abcClass : (Set<AbcClass>)gai.getWeavableClasses()) {
			SootClass sootClass = abcClass.getSootClass();
			for (SootMethod method : (List<SootMethod>)sootClass.getMethods()) {
				if(MethodCategory.weaveInside(method)) {
					if(method.hasActiveBody()) {
                        if(UNIQUE_ADVICE_ACTUALS) {
                            LocalSplitter.v().transform(method.getActiveBody());
                        }
						transform(method.getActiveBody());
					}
				}
			}
		}
	}
	
	//singleton pattern
	
	protected static TMShadowTagger instance;

	private TMShadowTagger() {}
	
	public static TMShadowTagger v() {
		if(instance==null) {
			instance = new TMShadowTagger();
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

/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Laurie Hendren
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

package abc.weaving.matching;

import static abc.weaving.matching.MethodAdviceList.State.FLUSHED;
import static abc.weaving.matching.MethodAdviceList.State.MODIFIED;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.util.ErrorInfo;
import soot.jimple.Stmt;
import abc.main.Main;
import abc.polyglot.util.ErrorInfoFactory;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.residues.NeverMatch;

/** The lists of {@link AdviceApplication} structures applying to a method
 *  @author Ganesh Sittampalam
 *  @author Laurie Hendren 
 */
public class MethodAdviceList {

    protected enum State {
        MODIFIED, FLUSHED
    };
    
    protected State state = MODIFIED;
    
	// find all members of an AdviceApplication list that have no successors
	// in the precedence ordering
	private static AdviceApplication noPreds(List<AdviceApplication> aalist) {
		List<AdviceApplication> results = new ArrayList<AdviceApplication>();
		AdviceApplication res = null;
		for (Iterator<AdviceApplication> it = aalist.iterator(); it.hasNext(); ) {
			res = it.next();
			boolean resHasPred = false;
			for (Iterator<AdviceApplication> it2 = aalist.iterator(); it2.hasNext() && !resHasPred; ) {
				AdviceApplication aa = it2.next();
				if (aa == res) continue;
				int prec = Main.v().getAbcExtension().getPrecedence(res.advice,aa.advice);
				resHasPred =    (prec == GlobalAspectInfo.PRECEDENCE_FIRST)
				             || (prec == GlobalAspectInfo.PRECEDENCE_CONFLICT);
			}
			if (!resHasPred)
				results.add(res);
		}
		if (!results.isEmpty()) {
			if (abc.main.options.OptionsParser.v().warn_prec_ambiguity()
					&& results.size() > 1)
		    		reportAmbiguousPrecedence(results);
			res = results.get(0);
			aalist.remove(res);
			return res;
		}
		if (!aalist.isEmpty())
			reportPrecedenceConflict(aalist);
		return null;
	}
	
	// topological sort in order of reverse precedence
	private static List<AdviceApplication> sortWithPrecedence(List<AdviceApplication> aalist) {
        //remove advice applications that never match
	    for (Iterator<AdviceApplication> iterator = aalist.iterator(); iterator.hasNext();) {
            AdviceApplication aa = iterator.next();
            if(NeverMatch.neverMatches(aa.getResidue())) {
                iterator.remove();
            }
        }

        //sort
		List<AdviceApplication> result = new ArrayList<AdviceApplication>();
		AdviceApplication start = noPreds(aalist);
		while (start != null) {
			result.add(start);
			start = noPreds(aalist);
		}
		return result;
	}
	
	

    private static void reportPrecedenceConflict(List<AdviceApplication> aaList) {
    	// FIXME: Should be a multiple position warning
    	String msg="";

    	msg+="Pieces of advice from ";
		
    	Iterator<AdviceApplication> it = aaList.iterator();
    	AdviceApplication aa = it.next();
    	msg += aa.advice.errorInfo();
		while (it.hasNext()) {
			aa = it.next();
			msg += " and " + aa.advice.errorInfo();
		}
		msg += " are in precedence conflict, and all apply here";
		
    	abc.main.Main.v().getAbcExtension().reportError
    	    (ErrorInfoFactory.newErrorInfo(ErrorInfo.SEMANTIC_ERROR,
    					   msg,
    					   aa.shadowmatch.getContainer(),
    					   aa.shadowmatch.getHost()));
    }
    
    private static void reportAmbiguousPrecedence(List<AdviceApplication> aaList) {
    	// FIXME: Should be a multiple position warning
    	String msg="";

    	msg+="Pieces of advice from ";
		
    	Iterator<AdviceApplication> it = aaList.iterator();
    	AdviceApplication aa = it.next();
    	msg += aa.advice.errorInfo();
		while (it.hasNext()) {
			aa = it.next();
			msg += " and " + aa.advice.errorInfo();
		}
		msg += " can be ordered arbitrarily, and all apply here";
		msg += " abc has chosen first as having lowest precedence";
		
    	abc.main.Main.v().getAbcExtension().reportError
    	    (ErrorInfoFactory.newErrorInfo(ErrorInfo.WARNING,
    					   msg,
    					   aa.shadowmatch.getContainer(),
    					   aa.shadowmatch.getHost()));
    }

    /** {@link AdviceApplication} structures are added to the list
     *  for one shadow, then the next etc. At each shadow, they need to
     *  be sorted in precedence order, so we sort them as they are added.
     *  This method should be called after each shadow to add the sorted
     *  list for that shadow to the main list for the method.
     */
    public void flush() {
        if(state==MODIFIED) {
        	bodyAdvice.addAll(sortWithPrecedence(bodyAdviceP));
            stmtAdvice.addAll(sortWithPrecedence(stmtAdviceP));
        	preinitializationAdvice.addAll(sortWithPrecedence(preinitializationAdviceP));
        	initializationAdvice.addAll(sortWithPrecedence(initializationAdviceP));
        	bodyAdviceP=new LinkedList<AdviceApplication>();
        	stmtAdviceP=new LinkedList<AdviceApplication>();
        	preinitializationAdviceP=new LinkedList<AdviceApplication>();
        	initializationAdviceP=new LinkedList<AdviceApplication>();
            state=FLUSHED;
        }
    }

    public List<AdviceApplication> bodyAdviceP=new LinkedList<AdviceApplication>();
    public List<AdviceApplication> stmtAdviceP=new LinkedList<AdviceApplication>();
    public List<AdviceApplication> preinitializationAdviceP=new LinkedList<AdviceApplication>();
    public List<AdviceApplication> initializationAdviceP=new LinkedList<AdviceApplication>();

    /** Advice that would apply to the whole body, i.e. at 
	execution joinpoints */
    public List<AdviceApplication> bodyAdvice=new LinkedList<AdviceApplication>();
    public void addBodyAdvice(AdviceApplication aa) {
	    bodyAdviceP.add(aa);
        state=MODIFIED;
    }

    /** Advice that would apply inside the body, i.e. most other joinpoints */
    public List<AdviceApplication> stmtAdvice=new LinkedList<AdviceApplication>();
    public void addStmtAdvice(AdviceApplication aa) {
	    stmtAdviceP.add(aa);
        state=MODIFIED;
    }

    /** pre-initialization joinpoints */
    public List<AdviceApplication> preinitializationAdvice
	=new LinkedList<AdviceApplication>();
    public void addPreinitializationAdvice(AdviceApplication aa) {
	    preinitializationAdviceP.add(aa);
        state=MODIFIED;
    }

    /** initialization joinpoints, trigger inlining of this() calls */
    public List<AdviceApplication> initializationAdvice=new LinkedList<AdviceApplication>();
    public void addInitializationAdvice(AdviceApplication aa) {
        initializationAdviceP.add(aa);
        state=MODIFIED;
    }

    /** returns true if there is no advice */
    public boolean isEmpty() { 
        flush();
        return(bodyAdvice.isEmpty() && 
	       stmtAdvice.isEmpty() &&
	       initializationAdvice.isEmpty() &&
               preinitializationAdvice.isEmpty());
    }

    /** returns true if there is any body advice */
    public boolean hasBodyAdvice() { 
        flush();
        return !bodyAdvice.isEmpty();
    }

    /** returns true if there is any stmt advice */
    public boolean hasStmtAdvice() {
        flush();
        return !stmtAdvice.isEmpty();
    }

    /** returns true if there is any initialization advice */
    public boolean hasInitializationAdvice() {
        flush();
        return !initializationAdvice.isEmpty();
    }

    /** returns true if there is any preinitialization advice */
    public boolean hasPreinitializationAdvice() {
        assert state==FLUSHED;
        return !preinitializationAdvice.isEmpty();
    }

    public String toString() {
	return "body advice: "+bodyAdvice+"\n"
	    +"statement advice: "+stmtAdvice+"\n"
	    +"preinitialization advice: "+preinitializationAdvice+"\n"
	    +"initialization advice: "+initializationAdvice+"\n";
    }

    public void debugInfo(String prefix,StringBuffer sb) {
	sb.append(prefix+"body advice:\n");
	Iterator<AdviceApplication> it;
	for(it=bodyAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
	sb.append(prefix+"stmt advice:\n");
	for(it=stmtAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
	sb.append(prefix+"preinit advice:\n");
	for(it=preinitializationAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
	sb.append(prefix+"init advice:\n");
	for(it=initializationAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
    }
    public List<AdviceApplication> allAdvice() {
        flush();
        List<AdviceApplication> ret = new ArrayList<AdviceApplication>();
        ret.addAll(bodyAdvice);
        ret.addAll(stmtAdvice);
        ret.addAll(preinitializationAdvice);
        ret.addAll(initializationAdvice);
        return ret;
    }

    /**
     * Reopens the methods advice list again for modifications, after it has been flushed.
     * Call {@link #flush()} again after modifications are done.
     */
    public void unflush() {
        flush();
        bodyAdviceP = new ArrayList<AdviceApplication>(bodyAdvice);
        initializationAdviceP = new ArrayList<AdviceApplication>(initializationAdvice);
        preinitializationAdviceP = new ArrayList<AdviceApplication>(preinitializationAdvice);
        stmtAdviceP = new ArrayList<AdviceApplication>(stmtAdvice);
        bodyAdvice.clear();
        initializationAdvice.clear();
        preinitializationAdvice.clear();
        stmtAdvice.clear();
        state = MODIFIED;
    }
    
    /**
     * Copies the given advice applciation so that it also applies at a new statement.
     * The new advice application will be a {@link StmtAdviceApplication} that does not support
     * around advice.
     * @param aa the original advice application
     * @param target the statement the copy applies to
     */
    public void copyAdviceApplication(AdviceApplication aa, Stmt target) {
        assert bodyAdviceP.contains(aa) || initializationAdviceP.contains(aa) ||
               preinitializationAdviceP.contains(aa) || stmtAdviceP.contains(aa);
        
        StmtShadowMatch newShadowMatch = new ReroutingShadowMatch(aa.shadowmatch,target);
        newShadowMatch.addAdviceApplication(this, aa.advice, aa.getResidue());
        
        state = MODIFIED;
    }
}

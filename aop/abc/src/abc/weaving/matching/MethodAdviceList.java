/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Laurie Hendren
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;

import abc.polyglot.util.ErrorInfoFactory;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbstractAdviceDecl;

/** The lists of {@link AdviceApplication} structures applying to a method
 *  @author Ganesh Sittampalam
 *  @author Laurie Hendren 
 */
public class MethodAdviceList {

    private static void addWithPrecedence(List/*<AdviceApplication>*/ aalist,
					  AdviceApplication aa) {

	// we want to sort the list in reverse order of precedence, so that
	// highest precedence advice gets applied *last*, i.e. outermost.
	// So we first look through the existing list until we get to a point
	// where the next piece of advice has higher precedence than the 
	// one we are adding. Before that advice we insert the new one,
	// and then go through the rest of the list checking there is no
	// circularity.

	ListIterator it=aalist.listIterator();
	while(it.hasNext()) {
	    AdviceApplication curaa=(AdviceApplication) (it.next());
	    int prec;
	    prec=AbstractAdviceDecl.getPrecedence(curaa.advice,aa.advice);
	    if(prec==GlobalAspectInfo.PRECEDENCE_CONFLICT) {
		reportPrecedenceConflict(curaa,aa);
	    }
	    if(prec==GlobalAspectInfo.PRECEDENCE_FIRST) {
		it.previous(); // for correct insertion
		break;
	    }
	}
	it.add(aa);
	while(it.hasNext()) {
	    AdviceApplication curaa=(AdviceApplication) (it.next());
	    int prec=AbstractAdviceDecl.getPrecedence(curaa.advice,aa.advice);
	    if(prec==GlobalAspectInfo.PRECEDENCE_CONFLICT 
	       || prec==GlobalAspectInfo.PRECEDENCE_SECOND) {
		reportPrecedenceConflict(curaa,aa);
		return;
	    }
	}
    }

    private static void reportPrecedenceConflict(AdviceApplication aa1,AdviceApplication aa2) {
	// FIXME: Should be a multiple position error
	String msg="";

	msg+="Pieces of advice from "+aa1.advice.errorInfo()
	    +" and "+aa2.advice.errorInfo()
	    +" are in precedence conflict, and both apply here";

	abc.main.Main.v().error_queue.enqueue
	    (ErrorInfoFactory.newErrorInfo(ErrorInfo.SEMANTIC_ERROR,
					   msg,
					   aa1.shadowmatch.getContainer(),
					   aa1.shadowmatch.getHost()));
    }

    /** {@link AdviceApplication} structures are added to the list
     *  for one shadow, then the next etc. At each shadow, they need to
     *  be sorted in precedence order, so we sort them as they are added.
     *  This method should be called after each shadow to add the sorted
     *  list for that shadow to the main list for the method.
     */
    public void flush() {
	bodyAdvice.addAll(bodyAdviceP);
	stmtAdvice.addAll(stmtAdviceP);
	preinitializationAdvice.addAll(preinitializationAdviceP);
	initializationAdvice.addAll(initializationAdviceP);
	bodyAdviceP=new LinkedList();
	stmtAdviceP=new LinkedList();
	preinitializationAdviceP=new LinkedList();
	initializationAdviceP=new LinkedList();
    }

    public List bodyAdviceP=new LinkedList();
    public List stmtAdviceP=new LinkedList();
    public List preinitializationAdviceP=new LinkedList();
    public List initializationAdviceP=new LinkedList();

    /** Advice that would apply to the whole body, i.e. at 
	execution joinpoints */
    public List/*<AdviceApplication>*/ bodyAdvice=new LinkedList();
    public void addBodyAdvice(AdviceApplication aa) {
	addWithPrecedence(bodyAdviceP,aa);
    }

    /** Advice that would apply inside the body, i.e. most other joinpoints */
    public List/*<AdviceApplication>*/ stmtAdvice=new LinkedList();
    public void addStmtAdvice(AdviceApplication aa) {
	addWithPrecedence(stmtAdviceP,aa);
    }

    /** pre-initialization joinpoints */
    public List/*<AdviceApplication>*/ preinitializationAdvice
	=new LinkedList();
    public void addPreinitializationAdvice(AdviceApplication aa) {
	addWithPrecedence(preinitializationAdviceP,aa);
    }

    /** initialization joinpoints, trigger inlining of this() calls */
    public List/*<AdviceApplication>*/ initializationAdvice=new LinkedList();
    public void addInitializationAdvice(AdviceApplication aa) {
	addWithPrecedence(initializationAdviceP,aa);
    }

    /** returns true if there is no advice */
    public boolean isEmpty() { 
        return(bodyAdvice.isEmpty() && 
	       stmtAdvice.isEmpty() &&
	       initializationAdvice.isEmpty() &&
               preinitializationAdvice.isEmpty());
    }

    /** returns true if there is any body advice */
    public boolean hasBodyAdvice() { 
      return !bodyAdvice.isEmpty();
    }

    /** returns true if there is any stmt advice */
    public boolean hasStmtAdvice() {
      return !stmtAdvice.isEmpty();
    }

    /** returns true if there is any initialization advice */
    public boolean hasInitializationAdvice() {
      return !initializationAdvice.isEmpty();
    }

    /** returns true if there is any preinitialization advice */
    public boolean hasPreinitializationAdvice() {
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
	Iterator it;
	for(it=bodyAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=(AdviceApplication) it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
	sb.append(prefix+"stmt advice:\n");
	for(it=stmtAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=(AdviceApplication) it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
	sb.append(prefix+"preinit advice:\n");
	for(it=preinitializationAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=(AdviceApplication) it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
	sb.append(prefix+"init advice:\n");
	for(it=initializationAdvice.iterator();it.hasNext();) {
	    final AdviceApplication aa=(AdviceApplication) it.next();
	    aa.debugInfo(prefix+" ",sb);
	}
    }
    public List/*AdviceApplication*/ allAdvice() {
        List/*AdviceApplication*/ ret = new ArrayList();
        ret.addAll(bodyAdvice);
        ret.addAll(stmtAdvice);
        ret.addAll(preinitializationAdvice);
        ret.addAll(initializationAdvice);
        return ret;
    }
}

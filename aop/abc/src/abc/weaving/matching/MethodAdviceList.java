package abc.weaving.matching;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbstractAdviceDecl;

/** The list(s) of advice applying to a method
 *  @author Ganesh Sittampalam
 *  @author Laurie Hendren 
 *       (added "isEmpty and has* methods", May 4, 2004)
 *  @date 28-Apr-04
 */
public class MethodAdviceList {

    private static void addWithPrecedence(List/*<AdviceApplication>*/ aalist,
				     AdviceApplication aa) {

	ListIterator it=aalist.listIterator();
	while(it.hasNext()) {
	    AdviceApplication curaa=(AdviceApplication) (it.next());
	    int prec;
	    // try {
	    prec=AbstractAdviceDecl.getPrecedence(curaa.advice,aa.advice);
	    /*} catch(RuntimeException e) {
	      StringBuffer details=new StringBuffer();
	      curaa.debugInfo("current: ",details);
	      aa.debugInfo("new: ",details);
	      System.err.println(details);
	      throw e;
	      }*/
	    if(prec==GlobalAspectInfo.PRECEDENCE_CONFLICT)
		// FIXME to SemanticException with more info
		throw new RuntimeException("Precedence conflict");
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
	       || prec==GlobalAspectInfo.PRECEDENCE_SECOND)
		// FIXME to SemanticException with more info
		throw new RuntimeException("Precedence conflict");
	}
    }

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
}

package abc.weaving.matching;

import java.util.List;
import java.util.LinkedList;


/** The list(s) of advice applying to a method
 *  @author Ganesh Sittampalam
 *  @author Laurie Hendren 
 *       (added "isEmpty and has* methods", May 4, 2004)
 *  @date 28-Apr-04
 */
public class MethodAdviceList {

    /** Advice that would apply to the whole body, i.e. at execution joinpoints */
    public List/*<AdviceApplication>*/ bodyAdvice=new LinkedList();

    /** Advice that would apply inside the body, i.e. most other joinpoints */
    public List/*<AdviceApplication>*/ stmtAdvice=new LinkedList();

    /** initialization or pre-initialization joinpoints, trigger inlining of this() calls */
    public List/*<AdviceApplication>*/ constructorAdvice=new LinkedList();

    /** returns true if there is no advice */
    public boolean isEmpty() { 
        return(bodyAdvice.isEmpty() && 
	       stmtAdvice.isEmpty() &&
	       constructorAdvice.isEmpty());
    }

    /** returns true if there is any body advice */
    public boolean hasBodyAdvice() { 
      return !bodyAdvice.isEmpty();
    }

    /** returns true if there is any stmt advice */
    public boolean hasStmtAdvice() {
      return !stmtAdvice.isEmpty();
    }

    /** returns true if there is any constructor advice */
    public boolean hasConstructorAdvice() {
      return !constructorAdvice.isEmpty();
    }

    public String toString() {
	return "body advice: "+bodyAdvice+"\n"
	    +"statement advice: "+stmtAdvice+"\n"
	    +"constructor advice: "+constructorAdvice+"\n";
    }
}

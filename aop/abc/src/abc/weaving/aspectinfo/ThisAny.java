package abc.weaving.aspectinfo;

import java.util.*;
import soot.*;
import polyglot.util.Position;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>this</code> condition pointcut with a 
    universal pattern argument. 
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */
public class ThisAny extends DynamicValuePointcut {

    public ThisAny(Position pos) {
	super(pos);
    }

    public final Residue matchesAt(WeavingEnv we,
				   SootClass cls,
				   SootMethod method,
				   ShadowMatch sm) {
	ContextValue cv=sm.getThisContextValue();
	if(cv==null) return null;
	return matchesAt(we,cv);
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return AlwaysMatch.v;
    }

    public String toString() {
	return "this(*)";
    }
    public void registerSetupAdvice
	(Aspect aspect,Hashtable/*<String,AbcType>*/ typeMap) {}
    public void getFreeVars(Set/*<String>*/ result) {}
}

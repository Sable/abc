package abc.weaving.matching;

import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** A weaving environment that handles locally scoped named pointcut variables
 *  @author Ganesh Sittampalam
 */

public class LocalsDecl implements WeavingEnv {
    private Hashtable/*<String,AbcType>*/ typeEnv;
    private Hashtable/*<String,LocalVar>*/ varEnv;
    private WeavingEnv child;

    public LocalsDecl(List/*<Formal>*/ formals,WeavingEnv child) {
	this.child=child;
	typeEnv=new Hashtable();
	varEnv=new Hashtable();
	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal f=(Formal) it.next();
	    typeEnv.put(f.getName(),f.getType());
	    varEnv.put(f.getName(),new LocalVar(f.getType().getSootType(),f.getName()));
	}
	
    }

    public WeavingVar getWeavingVar(Var v) {
	if(varEnv.containsKey(v.getName()))
	    return (LocalVar) varEnv.get(v.getName());
	else return child.getWeavingVar(v);
    }

    public AbcType getAbcType(Var v) {
	if(typeEnv.containsKey(v.getName()))
	    return (AbcType) typeEnv.get(v.getName());
	else return child.getAbcType(v);
    }
}

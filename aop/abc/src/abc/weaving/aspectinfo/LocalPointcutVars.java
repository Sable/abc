package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import polyglot.types.SemanticException;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Declare local pointcut variables. These can appear
 *  after inlining
 *  @author Ganesh Sittampalam
 */
public class LocalPointcutVars extends Pointcut {
    private Pointcut pc;
    private List/*<Formal>*/ formals;

    public LocalPointcutVars(Pointcut pc,List/*<Formal>*/ formals, Position pos) {
	super(pos);
	this.pc = pc;
	this.formals = formals;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public List getFormals() {
	return formals;
    }


    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) 
	throws SemanticException
    {
	WeavingEnv lwe=new LocalsDecl(formals,we);
	return pc.matchesAt(lwe,cls,method,sm);
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Pointcut pc=this.pc.inline(renameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new LocalPointcutVars(pc,formals,getPosition());
    }

    public String toString() {
	return "local"+formals+" ("+pc+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	pc.registerSetupAdvice(context,typeMap);
    }

    public void getFreeVars(Set/*<String>*/ result) {
	pc.getFreeVars(result);
	Iterator it=formals.iterator();
	while(it.hasNext()) result.remove(((Formal) (it.next())).getName());
    }

}

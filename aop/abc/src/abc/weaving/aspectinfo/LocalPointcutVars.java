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

	List/*<Formal>*/ newFormals=new ArrayList(formals.size());

	Hashtable newRenameEnv=new Hashtable(renameEnv);

	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal old=(Formal) it.next();

	    String newName=Pointcut.freshVar();
	    newFormals.add(new Formal(old.getType(),newName,old.getPosition()));

	    newRenameEnv.put(old.getName(),new Var(newName,old.getPosition()));
	}
	
	Pointcut pc=this.pc.inline(newRenameEnv,typeEnv,context);
	if(pc==this.pc) return this;
	else return new LocalPointcutVars(pc,newFormals,getPosition());
    }

    protected DNF dnf() {
	return DNF.declare(pc.dnf(),formals);
    }



    public String toString() {
	return "local"+formals+" ("+pc+")";
    }

    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	Hashtable newTypeMap=new Hashtable(typeMap);
	Iterator it=formals.iterator();
	while(it.hasNext()) {
	    Formal f=(Formal) it.next();
	    newTypeMap.put(f.getName(),f.getType());
	}
	pc.registerSetupAdvice(context,newTypeMap);
    }

    public void getFreeVars(Set/*<String>*/ result) {
	pc.getFreeVars(result);
	Iterator it=formals.iterator();
	while(it.hasNext()) result.remove(((Formal) (it.next())).getName());
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof LocalPointcutVars) {
	    if (!pc.equals(((LocalPointcutVars)otherpc).getPointcut())) 
		return false;
	    List/*<Formal>*/ otherformals = ((LocalPointcutVars)otherpc).getFormals();
	    return otherformals.equals(formals);
	} else return false;
    }

}

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
	    if (!pc.equivalent(((LocalPointcutVars)otherpc).getPointcut())) 
		return false;
	    List/*<Formal>*/ otherformals = ((LocalPointcutVars)otherpc).getFormals();
	    return otherformals.equals(formals);
	} else return false;
    }



	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof LocalPointcutVars) {
			LocalPointcutVars other = (LocalPointcutVars) otherpc; 
			if (pc.equivalent(other.getPointcut(), renaming)) {
				// The inner pcs are equivalent, and we have the renaming
				// Are the variables to be abstracted the same?
				// ie require that corresponding elements in the lists of formals:
				//   - have the same type
				//   - are related by the substitution
				
				Iterator it1 = formals.iterator();
				Iterator it2 = other.getFormals().iterator();
				while (it1.hasNext() && it2.hasNext()) {
					Formal form1 = (Formal) it1.next();
					Formal form2 = (Formal) it2.next();
					
					if (!form1.canRenameTo(form2, renaming)) 
							return false;	
				}
				if (it1.hasNext() || it2.hasNext()) return false;
				// The lists have the same length and corresponding elements are related
				// We are done
				return true;
			} else return false;
		} else return false;
	}

}

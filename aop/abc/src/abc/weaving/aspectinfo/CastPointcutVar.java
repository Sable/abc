package abc.weaving.aspectinfo;

import java.util.Hashtable;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.weaving.residues.Copy;



/** Cast from one pointcut variable to another. 
 *  This can appear after inlining
 *  @author Ganesh Sittampalam
 */
public class CastPointcutVar extends Pointcut {
    private Var from;
    private Var to;

    public CastPointcutVar(Var from,Var to,Position pos) {
	super(pos);
	this.from=from;
	this.to=to;
    }
    

    public Var getFrom() {
	return from;
    }

    public Var getTo() {
	return to;
    }

    public String toString() {
	return "cast("+from+","+to+")";
    }

    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) {
	// no need to cast, because the rules guarantee this is an upcast...
	return new Copy(we.getWeavingVar(from),we.getWeavingVar(to));
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Var from=this.from;
	if(renameEnv.containsKey(from.getName()))
	   from=(Var) renameEnv.get(from.getName());

	Var to=this.to;
	if(renameEnv.containsKey(to.getName()))
	   to=(Var) renameEnv.get(to.getName());

	if(from != this.from || to != this.to)
	    return new CastPointcutVar(from,to,getPosition());
	else return this;
	   
    }

}

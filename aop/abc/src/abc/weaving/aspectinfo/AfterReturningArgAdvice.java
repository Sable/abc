package abc.weaving.aspectinfo;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

import soot.*;

/** Advice specification for after returning advice with return variable binding. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class AfterReturningArgAdvice extends AfterReturningAdvice {
    private Formal formal;

    public AfterReturningArgAdvice(Formal formal, Position pos) {
	super(pos);
	this.formal = formal;
    }

    public Formal getFormal() {
	return formal;
    }

    public String toString() {
	return "after returning arg";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
	if(super.matchesAt(we,sm,ad)==null) return null;
	ContextValue cv=sm.getReturningContextValue();
	Var v=new Var(formal.getName(),formal.getPosition());
	return Bind.construct
	    (cv,we.getAbcType(v).getSootType(),we.getWeavingVar(v));
    }

}

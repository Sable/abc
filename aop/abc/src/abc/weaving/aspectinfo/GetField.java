package abc.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>get</code> shadow pointcut. */
public class GetField extends ShadowPointcut {
    private FieldPattern pattern;

    public GetField(FieldPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof GetFieldShadowMatch)) return null;
	SootFieldRef fieldref=((GetFieldShadowMatch) sm).getFieldRef();

	if(!getPattern().matchesFieldRef(fieldref)) return null;
	return AlwaysMatch.v;


	   
    }

    public String toString() {
	return "get("+pattern+")";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof GetField) {
			return pattern.equivalent(((GetField)otherpc).getPattern());
		} else return false;
	}

}

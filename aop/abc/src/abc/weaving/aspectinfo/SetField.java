package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>set</code> shadow pointcut. */
public class SetField extends ShadowPointcut {
    private FieldPattern pattern;

    public SetField(FieldPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof SetFieldShadowMatch)) return null;
	SootField field=((SetFieldShadowMatch) sm).getField();

	if(!getPattern().matchesField(field)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "set("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof SetField) {
	    return pattern.equivalent(((SetField)otherpc).getPattern());
	} else return false;
    }

}

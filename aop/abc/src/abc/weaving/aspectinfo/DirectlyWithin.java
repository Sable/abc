package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

public class DirectlyWithin extends Within {    
    public DirectlyWithin(ClassnamePattern pattern,Position pos) {
	super(pattern,pos);
    }

    protected Residue matchesAt(SootClass cls) {
	if(getPattern().matchesClass(cls)) return AlwaysMatch.v;
	return null;
    }
}
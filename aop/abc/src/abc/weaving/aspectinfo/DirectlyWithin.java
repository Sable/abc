package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for "directlywithin" condition pointcut. This pointcut is not
 *  supported in AspectJ source.
 *  The directlywithin(ClassPattern) pointcut matches any join point lexically contained
 *  within a class matching ClassPattern, that is not also contained within a nested class.
 *  @author Ganesh Sittampalam
 */

public class DirectlyWithin extends Within {    
    public DirectlyWithin(ClassnamePattern pattern,Position pos) {
	super(pattern,pos);
    }

    protected Residue matchesAt(SootClass cls) {
	if(getPattern().matchesClass(cls)) return AlwaysMatch.v;
	return null;
    }
}
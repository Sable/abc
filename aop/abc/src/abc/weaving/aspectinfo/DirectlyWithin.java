package abc.weaving.aspectinfo;

import java.util.Hashtable;

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
    
    
    
	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof DirectlyWithin) {
			return getPattern().equivalent(((DirectlyWithin)otherpc).getPattern());
		} else return false;
	}

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean equivalent(Pointcut otherpc) {
		if (otherpc instanceof DirectlyWithin) {
			return getPattern().equivalent(((DirectlyWithin)otherpc).getPattern());
		} else return false;
	}

}
package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.residues.Residue;

/** Handler for an instance of a specific kind of lexical context pointcut. */
public interface LexicalPointcutHandler {
    /** Checks whether a pointcut matches at this class+method */
    public Residue matchesAt(SootClass cls,SootMethod method);
}

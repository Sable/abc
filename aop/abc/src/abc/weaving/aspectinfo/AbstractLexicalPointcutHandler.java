package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.residues.Residue;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.LexicalPointcutHandler} interface.
 *  Useful when implementing lexical pointcut handlers.
 */
public abstract class AbstractLexicalPointcutHandler implements LexicalPointcutHandler {
    /* FIXME: remove this once all deriving classes implement it */
    public Residue matchesAt(SootClass cls,SootMethod method) {
    	System.out.println("Returning null for unimplemented lexical pointcut type "+this.getClass());
	return null;
    }
}

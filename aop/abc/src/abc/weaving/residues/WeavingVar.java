package abc.weaving.residues;

import soot.*;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A variable for use in weaving
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public abstract class WeavingVar {
    /** Set the variable to the value v and return the last statement added 
     *  (or begin if none)
     */
    public abstract Stmt set
	(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val);

    /** Get the soot local corresponding to this variable (only valid once it has been set) */
    public abstract Local get();

    /** Has this variable got a type yet? */
    public abstract boolean hasType();

    /** Get the soot type corresponding to this variable
     */
    public abstract Type getType();
    
    /** Should primitive typed values be boxed if necessary when writing to this variable? */
    public boolean maybeBox() {
	return getType().equals(Scene.v().getSootClass("java.lang.Object").getType());
    }

    /** Should we reject any binding value that isn't the appropriate type to box to this variable? */
    public boolean mustBox() {
	return false;
    }
    
}

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

public interface WeavingVar {
    /** Set the variable to the value v and return the last statement added 
     *  (or begin if none)
     */
    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val);

    /** Get the soot local corresponding to this variable (only valid once it has been set) */
    public Local get();

    /** Has this variable got a type yet? */
    public boolean hasType();

    /** Get the soot type corresponding to this variable
     */
    public Type getType();
    
}

package abc.weaving.residues;

import soot.Value;
import soot.Type;
import abc.weaving.weaver.WeavingContext;

/** A variable for use in weaving
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public interface WeavingVar {
    /** Set the element in the weaving context wc 
     *  corresponding to this variable to the value v 
     */
    public void set(WeavingContext wc,Value v);

    
    /** Get the soot type corresponding to this
     *  variable
     */
    public Type getType();
    
}

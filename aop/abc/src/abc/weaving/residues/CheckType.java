package abc.weaving.residues;

import soot.*;

/** Check the type of a context value
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class CheckType extends AbstractResidue {
    private ContextValue value;
    private Type type;

    public CheckType(ContextValue value,Type type) {
	this.value=value;
	this.type=type;
    }

}

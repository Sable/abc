package abc.weaving.residues;

import soot.*;

/** Bind a context value to a local or argument
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class Bind extends AbstractResidue {
    public ContextValue value;
    public WeavingVar variable;

    public Bind(ContextValue value,WeavingVar variable) {
	this.value=value;
	this.variable=variable;
    }

    public String toString() {
	return "bind("+value+","+variable+")";
    }

}

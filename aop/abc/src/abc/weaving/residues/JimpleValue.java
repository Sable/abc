package abc.weaving.residues;

import polyglot.util.InternalCompilerError;
import soot.*;
import abc.soot.util.LocalGeneratorEx;


/** A context value that comes directly from a 
 *  jimple value already in the current method
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class JimpleValue extends ContextValue {

    private Value value;

    public JimpleValue(Value value) {
	if(value==null) 
	    throw new InternalCompilerError("JimpleValue constructed with null argument");
	this.value=value;
    }

    public String toString() {
	return "jimplevalue("+value+")";
    }

    public Type getSootType() {
	return value.getType();
    }

    public Value getSootValue() {
	return value;
    }

}

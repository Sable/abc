package abc.weaving.residues;

import soot.Value;
import soot.SootMethod;
import abc.soot.util.LocalGeneratorEx;


/** A context value that comes directly from a 
 *  jimple value already in the current method
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class JimpleValue extends ContextValue {

    private Value value;

    public JimpleValue(Value value) {
	this.value=value;
    }

    public String toString() {
	return "jimplevalue("+value+")";
    }

    public Value getSootValue() {
	return value;
    }

}

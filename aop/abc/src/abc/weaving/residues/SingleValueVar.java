package abc.weaving.residues;

import polyglot.util.InternalCompilerError;
import soot.Value;
import soot.Local;
import soot.Type;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.SingleValueWeavingContext;

/** For cases where there is just one value needed in the weaving context
 *  Don't ever use it in a context where a get() might be done on it
 *  @author Ganesh Sittampalam
 */ 

public class SingleValueVar implements WeavingVar {
    public Type type;
    public Value value;

    public SingleValueVar(Type type) {
	this.type=type;
    }

    public String toString() {
	return "singlevaluevar("+type+")";
    }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	((SingleValueWeavingContext) wc).value=val;
	return begin;
    }

    public Local get() {
	throw new InternalCompilerError("Can't read from a SingleValueVar");
    }

    public boolean hasType() {
	return true;
    }

    public Type getType() {
	return type;
    }

}


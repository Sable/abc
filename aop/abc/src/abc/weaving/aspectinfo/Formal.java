
package abc.weaving.aspectinfo;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;

import soot.*;

import java.util.*;

/** A formal parameter to a method or constructor. */
public class Formal extends Syntax {
    private AbcType type;
    private String name;

    public Formal(AbcType type, String name, Position pos) {
	super(pos);
	this.type = type;
	this.name = name;
	if(name==null) 
	    throw new InternalCompilerError("Constructing a formal with a null name");
	if(type==null) 
	    throw new InternalCompilerError("Constructing a formal with a null type");
    }

    public AbcType getType() {
	return type;
    }

    public String getName() {
	return name;
    }

    public String toString() {
	return type+" "+name;
    }

    public boolean equals(Object other) {
	if (!(other instanceof Formal)) return false;
	Formal of = (Formal)other;
	return type.equals(of.type);
    }

    public int hashCode() {
	return type.hashCode();
    }
}

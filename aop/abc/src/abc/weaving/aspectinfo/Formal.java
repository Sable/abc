
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
    
    public boolean canRenameTo(Formal f, Hashtable/*<Var, Var>*/ renaming) {    	
    	if (type.equals(f.getType())) {
    		// NOTE: the renaming maps Vars to Vars as this is what we need later
    		// Formals are NEVER added to the renaming, we only check that there already
    		// is a corresponding entry
    		// Var objects are compared by name, so it is OK to create two new Vars for the
    		// test
    		// TODO Check that it is OK that Formal.canRenameTo does not add bindings
    		
    		Var thisv = new Var(name, getPosition());
    		Var otherv = new Var(f.getName(), f.getPosition());
    		
    		if (renaming.containsKey(thisv)) {
    			Var previous = (Var)renaming.get(thisv);
    			if (previous.getName().equals(f.getName())) {
    				return true;
    			} else return false; // Existing match, wrong name 
    		} else return false;     // No match
    		} else return false;     // Wrong type
    }
}

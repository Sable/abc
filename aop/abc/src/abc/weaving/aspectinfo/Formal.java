
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
    
    public boolean canRenameTo(Formal f, Hashtable/*<String, Var>*/ renaming) {    	
    	if (type.equals(f.getType())) {
    		if (renaming.containsKey(name)) {
    			Var previous = (Var)renaming.get(name);
    			if (previous.getName().equals(f.getName())) {
    				return true;
    			} else return false;
    		} else {
    			// Construct a new Var with name f.name to map to
    			// FIXME Is it OK to create a new var in Formal.canRenameTo if necessary?
    			// Note: Will only ever do this if a local var is declared in a pc but not 
    			// actually used. Does this ever happen? 
    			// This should mean that the new var never gets used anyway...
    			Var newvar = new Var(f.getName(), f.getPosition());
    			renaming.put(name, newvar);
    			return true;
    		}
    	} else return false;
    }
}

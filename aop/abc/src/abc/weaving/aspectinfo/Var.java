package abc.weaving.aspectinfo;

import java.util.Hashtable;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** A pointcut variable. */
public class Var extends Syntax {
    private String name;
    //    private int index;

    public Var(String name, Position pos) {
	super(pos);
	this.name=name;
    }

    //    public Var(String name, int index, Position pos) {
    //	super(pos);
    //	this.name = name;
    //	this.index = index;
    //}

    public String getName() {
	return name;
    }

    public String toString() {
	return name;
    }

    public Var rename(Hashtable/*<String,Var>*/ env) {
	if(env.containsKey(name)) return (Var) env.get(name);
	else return this;
    }
    
    /* canRenameTo: given another var and a list of existing substitutions,
     * can we rename this var to the other (the renaming is ok if there is no
     * previous binding, or the existing binding is the one we would have added) 
     * The new binding is added to the mapping */

	public boolean canRenameTo(Var other, Hashtable/*<String,Var>*/ renaming) {
		if (renaming.containsKey(name)) {
			Var previous = (Var)renaming.get(name);
			return previous.equals(other);
		} else {
			renaming.put(name, other);
			return true;
		}
	}

    //public int getIndex() {
    //	return index;
    //}

    public boolean equals(Object o) {
	if (o instanceof Var) {
	    return ((Var)o).getName().equals(name);
	} else return false; 
    }

	public int hashCode() {
		return name.hashCode();
	}

}

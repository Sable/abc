
package abc.weaving.aspectinfo;

import polyglot.types.ClassType;
import polyglot.util.InternalCompilerError;

import soot.*;

/** A Java type handled by the ABC compiler. 
 *  @author Aske Simon Christensen
 */
public class AbcType {
    private soot.Type st;
    private polyglot.types.Type pt;

    AbcType(soot.Type st) {
	if (st == null) throw new RuntimeException("AbcType with null Soot type");
	this.st = st;
    }

    AbcType(polyglot.types.Type pt) {
	if (pt == null) throw new RuntimeException("AbcType with null Polyglot type");
	this.pt = pt;
    }

    public soot.Type getSootType() {
	if (st == null) {
	    //System.out.println("Getting soot type for "+pt);
	    if (pt instanceof ClassType) {
		try {
		    st = AbcFactory.classTypeToSootClass((ClassType)pt).getType();
		} catch (NullPointerException e) {
		    throw new InternalCompilerError("Soot type of AbcType "+pt+" not ready yet");
		}
	    } else {
		st = soot.javaToJimple.Util.getSootType(pt);
	    }
	}
	return st;
    }

    public String toString() {
	return getSootType().toString();
    }

    public boolean equals(Object other) {
	if (!(other instanceof AbcType)) return false;
	AbcType ot = (AbcType)other;
	if (pt != null && ot.pt != null)
	    return pt.equals(ot.pt);
	return getSootType().equals(ot.getSootType());
    }

    public int hashCode() {
	// No nontrivial valid hashcode possible
	return 0;
    }
}

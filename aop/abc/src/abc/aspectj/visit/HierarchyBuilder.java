
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import abc.aspectj.ExtensionInfo;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcFactory;

import java.util.*;

public class HierarchyBuilder extends NodeVisitor {
    private ExtensionInfo ext;
    private Map/*<ParsedClassType,String>*/ seen_classes_name = new HashMap();

    public HierarchyBuilder(ExtensionInfo ext) {
	this.ext = ext;
    }

    public static boolean isNameable(ClassType ct) {
	if (ct.kind() == ClassType.TOP_LEVEL) return true;
	if (ct.kind() == ClassType.MEMBER) return isNameable(ct.outer());
	return false;
    }

    public NodeVisitor enter(Node n) {
	boolean debug = abc.main.Debug.v().classKinds;
	if (n instanceof ClassDecl) {
	    ParsedClassType ct = ((ClassDecl)n).type();
	    ext.hierarchy.insertClassAndSuperclasses(ct, true);
	    String java_name = ct.fullName();
	    if (ct.kind() == ClassType.ANONYMOUS || ct.kind() == ClassType.LOCAL) {
		GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct));
		if (debug) System.err.println("Local class: "+java_name);
	    } else if (ct.kind() == ClassType.MEMBER) {
		ReferenceType cont = ct.container();
		if (seen_classes_name.containsKey(cont)) {
		    String cont_name = (String)seen_classes_name.get(cont);
		    String jvm_name = cont_name+"$"+ct.name();
		    GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct, java_name));
		    seen_classes_name.put(ct, jvm_name);
		    if (debug) System.err.println("Visible inner class: "+java_name+" ("+jvm_name+")");
		} else {
		    GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct));
		    if (debug) System.err.println("Invisible inner class: "+java_name);
		}
	    } else if (ct.kind() == ClassType.TOP_LEVEL) {
		GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct, java_name));
		seen_classes_name.put(ct, java_name);
		if (debug) System.err.println("Toplevel class: "+java_name);
	    }
	    
	}
	if (n instanceof New && ((New)n).body() != null) {
	    ParsedClassType ct = ((New)n).anonType();
	    ext.hierarchy.insertClassAndSuperclasses(ct, true);
	    GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct));
	    if (abc.main.Debug.v().classKinds) {
		String java_name = ct.fullName();
		if (debug) System.err.println("Anonymous class: "+java_name);
	    }
	}
	return this;
    }

}

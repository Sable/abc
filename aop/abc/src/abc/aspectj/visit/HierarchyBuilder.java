
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

import java.util.*;

public class HierarchyBuilder extends NodeVisitor {
    private PCStructure hierarchy;
    private Collection weavable_classes;
    private Map/*<ParsedClassType,String>*/ seen_classes_name = new HashMap();

    public HierarchyBuilder(PCStructure hierarchy, Collection weavable_classes) {
	this.hierarchy = hierarchy;
	this.weavable_classes = weavable_classes;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof ClassDecl) {
	    boolean debug = abc.main.Debug.v().classKinds;
	    ParsedClassType ct = ((ClassDecl)n).type();
	    String java_name = ct.fullName();
	    if (ct.kind() == ClassType.ANONYMOUS || ct.kind() == ClassType.LOCAL) {
		GlobalAspectInfo.v().addClass(new AbcClass(ct));
		if (debug) System.err.println("Local class: "+java_name);
	    } else if (ct.kind() == ClassType.MEMBER) {
		ReferenceType cont = ct.container();
		if (seen_classes_name.containsKey(cont)) {
		    String cont_name = (String)seen_classes_name.get(cont);
		    String jvm_name = cont_name+"$"+ct.name();
		    PCNode pc = hierarchy.insertClass(java_name, true);
		    setParents(pc, ct);
		    weavable_classes.add(jvm_name);
		    GlobalAspectInfo.v().addClass(new AbcClass(ct, java_name));
		    seen_classes_name.put(ct, jvm_name);
		    if (debug) System.err.println("Visible inner class: "+java_name+" ("+jvm_name+")");
		} else {
		    GlobalAspectInfo.v().addClass(new AbcClass(ct));
		    if (debug) System.err.println("Invisible inner class: "+java_name);
		}
	    } else if (ct.kind() == ClassType.TOP_LEVEL) {
		PCNode pc = hierarchy.insertClass(java_name, true);
		setParents(pc, ct);
		weavable_classes.add(java_name);
		GlobalAspectInfo.v().addClass(new AbcClass(ct, java_name));
		seen_classes_name.put(ct, java_name);
		if (debug) System.err.println("Toplevel class: "+java_name);
	    }
	    
	}
	if (n instanceof New && ((New)n).body() != null) {
	    ParsedClassType ct = ((New)n).anonType();
	    GlobalAspectInfo.v().addClass(new AbcClass(ct));
	    if (abc.main.Debug.v().classKinds) {
		String java_name = ct.fullName();
		System.err.println("Anonymous class: "+java_name);
	    }
	}
	return this;
    }

    private void setParents(PCNode pc, ClassType t) {
	ClassType st = (ClassType)t.superType();
	if (st != null) {
	    PCNode scpc = hierarchy.insertClass(st.fullName(), false);
	    pc.addParent(scpc);
	    setParents(scpc, (ClassType)st);
	}
	Iterator iii = t.interfaces().iterator();
	while (iii.hasNext()) {
	    ClassType ii = (ClassType)iii.next();
	    PCNode iipc = hierarchy.insertClass(ii.fullName(), false);
	    pc.addParent(iipc);
	    setParents(iipc, (ClassType)ii);
	}

    }

}


package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class CollectJimplifyVisitor extends NodeVisitor {
    private Map class_to_ast;
    private PCStructure hierarchy;

    private Node current_ast;

    public CollectJimplifyVisitor(Map class_to_ast, PCStructure hierarchy) {
	this.class_to_ast = class_to_ast;
	this.hierarchy = hierarchy;
    }

    public Node override(Node n) {
	if (n instanceof SourceFile) {
            current_ast = n;
	}
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).type().fullName();
	    cname = hierarchy.transformClassName(cname);
            class_to_ast.put(cname, current_ast);
	    return n;
        }
	return null;
    }

}



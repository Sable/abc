
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;

import java.util.*;

public class CleanAspectMembers extends NodeVisitor {
    private NodeFactory nf;

    public CleanAspectMembers(NodeFactory nf) {
	this.nf = nf;
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	if (n instanceof AdviceDecl) {
	    AdviceDecl ad = (AdviceDecl)n;
	    System.out.println("Cleaning out an advice declaration");
	    return nf.MethodDecl(ad.position(), ad.flags(), ad.returnType(), ad.name(),
				 ad.formals(), ad.throwTypes(), ad.body())
		.methodInstance(ad.methodInstance());
	}
	if (n instanceof ClassDecl) {
	    ClassDecl cd = (ClassDecl)n;
	    List members = cd.body().members();
	    List newmembers = new ArrayList();
	    Iterator mi = members.iterator();
	    while (mi.hasNext()) {
		ClassMember m = (ClassMember)mi.next();
		if (m instanceof AdviceDecl) {
		    throw new RuntimeException("Advice declaration not cleaned up");
		}
		if (m instanceof IntertypeDecl ||
		    m instanceof DeclareDecl ||
		    m instanceof PointcutDecl) {
		    System.out.println("Cleaning out a node of type "+m.getClass());
		    // This must be removed
		} else {
		    newmembers.add(m);
		}
	    }
	    return nf.ClassDecl(cd.position(), cd.flags(), cd.name(), cd.superClass(), cd.interfaces(),
				nf.ClassBody(cd.body().position(), newmembers))
		.type(cd.type());
	}
	return n;
    }

}

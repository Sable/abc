
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.frontend.Source;

import abc.aspectj.ast.*;

import java.util.*;

public class AspectNameCollector extends NodeVisitor {
    private Collection/*<String>*/ aspect_names;

    public AspectNameCollector(Collection/*<String>*/ aspect_names) {
	this.aspect_names = aspect_names;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof AspectDecl) {
	    String aname = ((AspectDecl)n).type().fullName();
	    aspect_names.add(aname);
        }
	return this;
    }

}

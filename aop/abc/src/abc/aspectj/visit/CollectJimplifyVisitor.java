
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.frontend.Source;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class CollectJimplifyVisitor extends ErrorHandlingVisitor {
    private Collection/*<String>*/ source_files;
    private Map class_to_ast;
    private PCStructure hierarchy;

    private Node current_ast;

    public CollectJimplifyVisitor(Job job, TypeSystem ts, NodeFactory nf,
				  Collection/*<String>*/ source_files,
				  Map class_to_ast, PCStructure hierarchy) {
	super(job, ts, nf);
	this.source_files = source_files;
	this.class_to_ast = class_to_ast;
	this.hierarchy = hierarchy;
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
	if (n instanceof SourceFile) {
	    if (!source_files.contains(((SourceFile)n).source().name())) {
		throw new SemanticException("Source file was needed but not given on the commandline", n.position());
	    }
            current_ast = n;
	}
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).type().fullName();
	    cname = hierarchy.transformClassName(cname);
            class_to_ast.put(cname, current_ast);
	    return bypass(n);
        }
	return this;
    }

}



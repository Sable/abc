
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import java.util.*;

public class ParentDeclarer extends ErrorHandlingVisitor {
    private PCStructure hierarchy;
    private Collection weavable_classes;

    public ParentDeclarer(Job job, TypeSystem ts, NodeFactory nf,
			  PCStructure hierarchy, Collection weavable_classes) {
	super(job, ts, nf);
	this.hierarchy = hierarchy;
	this.weavable_classes = weavable_classes;
    }

    public NodeVisitor enterCall(Node n) throws SemanticException {
	//TODO: More sanity checks

	if (n instanceof DeclareParentsImpl) {
	    ClassnamePatternExpr pat = ((DeclareParentsImpl)n).pat();
	    List/*<TypeNde>*/ interfaces = ((DeclareParentsImpl)n).interfaces();
	    Iterator cli = weavable_classes.iterator();
	    while (cli.hasNext()) {
		String cl = (String)cli.next();
		if (PatternMatcher.v().matchesClass(pat, cl)) {
		    // FIXME: Check that cl is a class
		    ClassType ct = typeSystem().typeForName(cl);
		    PCNode hi_cl = hierarchy.getClass(cl);
		    if (ct instanceof ParsedClassType) {
			ParsedClassType pct = (ParsedClassType)ct;
			Iterator ini = interfaces.iterator();
			while (ini.hasNext()) {
			    TypeNode in = (TypeNode)ini.next();
			    if (!in.type().isClass()) {
				//FIXME: Check that in is an interface
				throw new SemanticException("Type "+in+" is not an interface");
			    }
			    ClassType inct = (ClassType)in.type();
			    PCNode hi_in = hierarchy.getClass(inct.fullName());

			    //System.err.println("Declared "+cl+" to implement "+inct.fullName());

			    pct.addInterface(inct);
			    hi_cl.addParent(hi_in);
			}
		    }
		}
	    }
	}

	if (n instanceof DeclareParentsExt) {
	    Type object_type = typeSystem().typeForName("java.lang.Object");
	    ClassnamePatternExpr pat = ((DeclareParentsExt)n).pat();
	    TypeNode type = ((DeclareParentsExt)n).type();
	    Iterator cli = weavable_classes.iterator();
	    while (cli.hasNext()) {
		String cl = (String)cli.next();
		if (PatternMatcher.v().matchesClass(pat, cl)) {
		    ClassType ct = typeSystem().typeForName(cl);
		    PCNode hi_cl = hierarchy.getClass(cl);
		    if (ct instanceof ParsedClassType) {
			ParsedClassType pct = (ParsedClassType)ct;
			if (!pct.superType().equals(object_type)) {
			    throw new SemanticException("Class "+cl+" already has a superclass");
			}
			if (!type.type().isClass()) {
			    //FIXME: Check that cl and type are either both classes or both intrfaces
			    throw new SemanticException("Type "+type+" is not an class");
			}
			ClassType typect = (ClassType)type.type();
			PCNode hi_type = hierarchy.getClass(typect.fullName());

			//System.err.println("Declared "+cl+" to extend "+typect.fullName());

			pct.superType(typect);
			hi_cl.addParent(hi_type);
		    }
		}
	    }
	}

	return this;
    }

}

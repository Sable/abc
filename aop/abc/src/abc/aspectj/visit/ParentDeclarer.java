
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import java.util.*;

public class ParentDeclarer extends NodeVisitor {
    private PCStructure hierarchy;
    private Collection weavable_classes;
    private TypeSystem ts;

    public ParentDeclarer(PCStructure hierarchy, Collection weavable_classes, TypeSystem ts) {
	this.hierarchy = hierarchy;
	this.weavable_classes = weavable_classes;
	this.ts = ts;
    }

    public NodeVisitor enter(Node n) {
	//TODO: More Sanity check
	try {
	//FIXME: Should really report an error instead of throwing a SemanticException. How?
	if (n instanceof DeclareParentsImpl) {
	    ClassnamePatternExpr pat = ((DeclareParentsImpl)n).pat();
	    List/*<TypeNde>*/ interfaces = ((DeclareParentsImpl)n).interfaces();
	    Iterator cli = weavable_classes.iterator();
	    while (cli.hasNext()) {
		String cl = (String)cli.next();
		if (PatternMatcher.v().matchesClass(pat, cl)) {
		    ClassType ct = ts.typeForName(cl);
		    PCNode hi_cl = hierarchy.insertFullName(cl, true, true);
		    if (ct instanceof ParsedClassType) {
			ParsedClassType pct = (ParsedClassType)ct;
			Iterator ini = interfaces.iterator();
			while (ini.hasNext()) {
			    TypeNode in = (TypeNode)ini.next();
			    if (!in.type().isClass()) {
				//FIXME: How do you check if it is an interface???
				throw new SemanticException("Type "+in+" is not an interface");
			    }
			    ClassType inct = (ClassType)in.type();
			    PCNode hi_in = hierarchy.insertFullName(inct.fullName(), true, false);

			    System.err.println("Declared "+cl+" to implement "+inct.fullName());

			    pct.addInterface(inct);
			    hi_cl.addParent(hi_in);
			}
		    }
		}
	    }
	}
	if (n instanceof DeclareParentsExt) {
	    Type object_type = ts.typeForName("java.lang.Object");
	    ClassnamePatternExpr pat = ((DeclareParentsExt)n).pat();
	    TypeNode type = ((DeclareParentsExt)n).type();
	    Iterator cli = weavable_classes.iterator();
	    while (cli.hasNext()) {
		String cl = (String)cli.next();
		if (PatternMatcher.v().matchesClass(pat, cl)) {
		    ClassType ct = ts.typeForName(cl);
		    PCNode hi_cl = hierarchy.insertFullName(cl, true, true);
		    if (ct instanceof ParsedClassType) {
			ParsedClassType pct = (ParsedClassType)ct;
			if (!pct.superType().equals(object_type)) {
			    throw new SemanticException("Class "+cl+" already has a superclass");
			}
			if (!type.type().isClass()) {
			    //FIXME: How do you check if it is not an interface???
			    throw new SemanticException("Type "+type+" is not an class");
			}
			ClassType typect = (ClassType)type.type();
			PCNode hi_type = hierarchy.insertFullName(typect.fullName(), true, false);

			System.err.println("Declared "+cl+" to extend "+typect.fullName());

			pct.superType(typect);
			hi_cl.addParent(hi_type);
		    }
		}
	    }
	}

	} catch (SemanticException e) {
	    throw new RuntimeException(e);
	}

	return this;
    }

}

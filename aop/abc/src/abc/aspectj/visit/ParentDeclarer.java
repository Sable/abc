
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import abc.aspectj.ExtensionInfo;

import java.util.*;

public class ParentDeclarer extends ErrorHandlingVisitor {
    private ExtensionInfo ext;

    public ParentDeclarer(Job job, TypeSystem ts, NodeFactory nf,
			  ExtensionInfo ext) {
	super(job, ts, nf);
	this.ext = ext;
    }

    public NodeVisitor enterCall(Node n) throws SemanticException {
	//TODO: More sanity checks

	if (n instanceof DeclareParentsImpl) {
	    ClassnamePatternExpr pat = ((DeclareParentsImpl)n).pat();
	    List/*<TypeNde>*/ interfaces = ((DeclareParentsImpl)n).interfaces();
	    Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
	    while (cti.hasNext()) {
		ClassType ct = (ClassType)cti.next();
		PCNode hi_cl = ext.hierarchy.getClass(ct);
		if (pat.matches(PatternMatcher.v(), hi_cl)) {
		    if (ct instanceof ParsedClassType) {
			ParsedClassType pct = (ParsedClassType)ct;
			Iterator ini = interfaces.iterator();
			while (ini.hasNext()) {
			    TypeNode in = (TypeNode)ini.next();
			    if (in.type().isClass()) {
				ClassType ict = (ClassType)in.type();
				if (!ict.flags().isInterface()) {
				    throw new SemanticException("Type "+in+" is not an interface");
				}
			    } else {
				throw new SemanticException("Type "+in+" is not a class");
			    }
			    ClassType inct = (ClassType)in.type();
			    PCNode hi_in = ext.hierarchy.insertClassAndSuperclasses(inct, false);
			    
			    //System.err.println("Declared "+ct.fullName()+" to implement "+inct.fullName());
			    
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
	    //FIXME: Check that type is a class
	    Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
	    while (cti.hasNext()) {
		ClassType ct = (ClassType)cti.next();
		PCNode hi_cl = ext.hierarchy.getClass(ct);
		if (pat.matches(PatternMatcher.v(), hi_cl)) {
		    if (ct instanceof ParsedClassType) {
			ParsedClassType pct = (ParsedClassType)ct;
			if (!pct.superType().equals(object_type)) {
			    throw new SemanticException("Class "+ct+" already has a superclass");
			}
			if (!type.type().isClass()) {
			    throw new SemanticException("Type "+type+" is not an class");
			}
			ClassType typect = (ClassType)type.type();
			PCNode hi_type = ext.hierarchy.insertClassAndSuperclasses(typect, false);
			
			//System.err.println("Declared "+ct.fullName()+" to extend "+typect.fullName());
			
			pct.superType(typect);
			hi_cl.addParent(hi_type);
		    }
		}
	    }
	}

	return this;
    }

}


package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import abc.aspectj.ExtensionInfo;

import abc.weaving.aspectinfo.AbcFactory;

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

	if (n instanceof DeclareParents) {
	    DeclareParents dp = (DeclareParents)n;
	    ClassnamePatternExpr pat = dp.pat();
	    List/*<TypeNode>*/ parents = dp.parents();

	    if (parents.size() == 1 && !((ClassType)((TypeNode)parents.get(0)).type()).flags().isInterface()) {
		// Extending a singe class

		ClassType parentct = (ClassType)((TypeNode)parents.get(0)).type();
		if (dp.kind() != DeclareParents.EXTENDS) {
		    throw new SemanticException("Type "+parentct+" is not an interface");
		}

		Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
		while (cti.hasNext()) {
		    ClassType ct = (ClassType)cti.next();
		    PCNode hi_cl = ext.hierarchy.getClass(ct);
		    if (hi_cl.isWeavable() && pat.matches(PatternMatcher.v(), hi_cl)) {
			dp.addTarget(AbcFactory.AbcClass(ct));
			if (ct instanceof ParsedClassType) {
			    ParsedClassType pct = (ParsedClassType)ct;
			    /* FIXME: What are the exact type rules here?
			       if (!ts.isSubtype(type.type(), pct.superType())) {
			       throw new SemanticException("Declared parent class "+type.type()+
			       " is not a subclass of original superclass "+pct.superType());
			       }
			       if (ts.isSubtype(type.type(), pct)) {
			       throw new SemanticException("Declared parent class "+type.type()+
			       " is a subclass of child class "+pct);
			       }
			    */
			    PCNode hi_parent = ext.hierarchy.insertClassAndSuperclasses(parentct, false);
			    
			    //System.err.println("Declared "+ct.fullName()+" to extend "+typect.fullName());
			    
			    pct.superType(parentct);
			    hi_cl.addParent(hi_parent);
			    //System.out.println(hi_cl+" extends "+hi_type);
			}
		    }
		}
		
	    } else {
		// Extending or implementing a list of interfaces
		List/*<ClassType>*/ ints = new ArrayList();
		Iterator pi = parents.iterator();
		while (pi.hasNext()) {
		    TypeNode p = (TypeNode)pi.next();
		    ClassType pct;
		    try {
			pct = (ClassType)p.type();
		    } catch (ClassCastException e) {
			throw new SemanticException("Type "+p.type()+" is not a class");
		    }
		    if (!pct.flags().isInterface()) {
			throw new SemanticException("Type "+pct+" is not an interface");
		    }
		    ints.add(pct);
		}

		Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
		while (cti.hasNext()) {
		    ClassType ct = (ClassType)cti.next();
		    PCNode hi_cl = ext.hierarchy.getClass(ct);
		    if (hi_cl.isWeavable() && pat.matches(PatternMatcher.v(), hi_cl)) {
			dp.addTarget(AbcFactory.AbcClass(ct));
			if (ct instanceof ParsedClassType) {
			    ParsedClassType pct = (ParsedClassType)ct;
			    Iterator incti = ints.iterator();
			    while (incti.hasNext()) {
				ClassType inct = (ClassType)incti.next();
				PCNode hi_in = ext.hierarchy.insertClassAndSuperclasses(inct, false);
				
				//System.err.println("Declared "+ct.fullName()+" to implement "+inct.fullName());
				
				pct.addInterface(inct);
				hi_cl.addParent(hi_in);
				//System.out.println(hi_cl+" implements "+hi_in);
			    }
			}
		    }
		}
	    }
	}

	return this;
    }

}


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

	if (n instanceof DeclareParentsImpl) {
	    DeclareParentsImpl dpi = (DeclareParentsImpl)n;
	    ClassnamePatternExpr pat = dpi.pat();
	    List/*<TypeNde>*/ interfaces = dpi.interfaces();
	    Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
	    while (cti.hasNext()) {
		ClassType ct = (ClassType)cti.next();
		PCNode hi_cl = ext.hierarchy.getClass(ct);
		if (hi_cl.isWeavable() && pat.matches(PatternMatcher.v(), hi_cl)) {
		    dpi.addTarget(AbcFactory.AbcClass(ct));
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
			    //System.out.println(hi_cl+" implements "+hi_in);
			}
		    }
		}
	    }
	}

	if (n instanceof DeclareParentsExt) {
	    DeclareParentsExt dpe = (DeclareParentsExt)n;
	    ClassnamePatternExpr pat = dpe.pat();
	    TypeNode type = dpe.type();
	    if (!type.type().isClass()) {
		throw new SemanticException("Type "+type+" is not an class");
	    }
	    //FIXME: Check that type is not an interface
	    Iterator cti = new ArrayList(ext.hierarchy.getClassTypes()).iterator();
	    while (cti.hasNext()) {
		ClassType ct = (ClassType)cti.next();
		PCNode hi_cl = ext.hierarchy.getClass(ct);
		if (hi_cl.isWeavable() && pat.matches(PatternMatcher.v(), hi_cl)) {
		    dpe.addTarget(AbcFactory.AbcClass(ct));
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
			ClassType typect = (ClassType)type.type();
			PCNode hi_type = ext.hierarchy.insertClassAndSuperclasses(typect, false);
			
			//System.err.println("Declared "+ct.fullName()+" to extend "+typect.fullName());
			
			pct.superType(typect);
			hi_cl.addParent(hi_type);
			//System.out.println(hi_cl+" extends "+hi_type);
		    }
		}
	    }
	}

	return this;
    }

}


package abc.aspectj.visit;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

import abc.aspectj.ExtensionInfo;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcFactory;

import java.util.*;

public class InitClasses extends OncePass {
    private ExtensionInfo ext;
    private TypeSystem ts;

    public InitClasses(Pass.ID id, ExtensionInfo ext, TypeSystem ts) {
	super(id);
	this.ext = ext;
	this.ts = ts;
    }

    public void once() {
	try {
	    Resolver res = ts.loadedResolver();

	    ext.hierarchy = new PCStructure(res);

	    // Fetch all the weavable classes and put them in the right places
	    Iterator wcni = ext.jar_classes.iterator();
	    while (wcni.hasNext()) {
		String wcn = (String)wcni.next();
		ClassType ct = (ClassType)res.find(wcn);
		if (ct == null) {
		    throw new InternalCompilerError("Class type of jar class was null");
		}
		ext.hierarchy.insertClassAndSuperclasses(ct, true);
		ext.hierarchy.registerName(ct, wcn);
		GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct));
	    }

	    GlobalAspectInfo.v().initPrecedenceRelation(ext.prec_rel);
	    
	    ext.pattern_matcher = PatternMatcher.create(ext.hierarchy);
	} catch (SemanticException e) {
	    throw new InternalCompilerError("Class from jar not found by Polyglot");
	}
    }
}


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

public class InitClasses extends AbstractPass {
    private ExtensionInfo ext;
    private TypeSystem ts;

    private boolean has_been_run = false;

    public InitClasses(Pass.ID id, ExtensionInfo ext, TypeSystem ts) {
	super(id);
	this.ext = ext;
	this.ts = ts;
    }

    public boolean run() {
	try {
	    if (!has_been_run) {
		ext.hierarchy = new PCStructure(ts.systemResolver());

		// Fetch all the weavable classes and put them in the right places
		Resolver res = ts.systemResolver();
		Iterator wcni = ext.jar_classes.iterator();
		while (wcni.hasNext()) {
		    String wcn = (String)wcni.next();
		    ClassType ct = (ClassType)res.find(wcn);
		    if (ct == null) {
			throw new InternalCompilerError("Class type of jar class was null");
		    }
		    ext.hierarchy.insertClassAndSuperclasses(ct, true);
		    GlobalAspectInfo.v().addWeavableClass(AbcFactory.AbcClass(ct));
		}

		GlobalAspectInfo.v().initPrecedenceRelation(ext.prec_rel);

		ext.pattern_matcher = PatternMatcher.create(ext.hierarchy);

		has_been_run = true;
	    }
	    return true;
	} catch (SemanticException e) {
	    throw new InternalCompilerError("Class from jar not found by Polyglot");
	}
    }
}

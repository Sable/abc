
package abc.aspectj.extension;

import polyglot.ast.Disamb;
import polyglot.ast.Receiver;
import polyglot.ast.Special;

import polyglot.types.SemanticException;
import polyglot.types.FieldInstance;
import polyglot.types.ClassType;

import polyglot.ext.jl.ast.Disamb_c;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;

/**
 when creating missing field targets, check whether this is a reference
 to an intertype host.
 */

public class AJDisamb_c extends Disamb_c implements Disamb {
	
	protected Receiver makeMissingFieldTarget(FieldInstance fi) throws SemanticException {
			Receiver r;

			if (fi.flags().isStatic()) {
				r = nf.CanonicalTypeNode(pos, fi.container());
			} else {
				// The field is non-static, so we must prepend with
				// "this", but we need to determine if the "this"
				// should be qualified.  Get the enclosing class which
				// brought the field into scope.  This is different
				// from fi.container().  fi.container() returns a super
				// type of the class we want.
	
			
				
				AJTypeSystem ajts = (AJTypeSystem) ts;
				AspectJNodeFactory ajnf = (AspectJNodeFactory) nf;
				// first check whether this is a reference to the host of an ITD
				if (ajts.refHostOfITD((AJContext)c,fi)) {
					AJContext ajc = (AJContext) c;
					ClassType scope = ajc.findFieldScopeInHost(name);
					if (! ts.equals(scope,ajc.hostClass()))
						r = ajnf.hostSpecial(pos,Special.THIS,nf.CanonicalTypeNode(pos,scope),
						                                    ajc.hostClass());
					else
					    r = ajnf.hostSpecial(pos,Special.THIS,null, ((AJContext)c).hostClass());
				} else
				{ 	ClassType scope = c.findFieldScope(name);
					if (! ts.equals(scope, c.currentClass())) {
						r = nf.This(pos, nf.CanonicalTypeNode(pos, scope));
					} else {
						r = nf.This(pos);
					}
				}
			}

			return r;
		}
}


package abc.aspectj.visit;

import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.visit.AmbiguityRemover;

public class DeclareParentsAmbiguityRemover extends AmbiguityRemover {

    public static class NewKind extends Kind {
	protected NewKind(String name) {
	    super(name);
	}
    }

    public static final Kind DECLARE = new NewKind("disam-declare");

    public DeclareParentsAmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf, Kind kind) {
        super(job, ts, nf, kind);
    }

}


package abc.aspectj.visit;

import abc.aspectj.ast.*;
import abc.weaving.aspectinfo.*;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.types.*;
import polyglot.frontend.*;

import java.util.*;

public class AspectInfoHarvester extends ContextVisitor {
    private GlobalAspectInfo gai;
    private String current_aspect_name;
    private Aspect current_aspect;

    public AspectInfoHarvester(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
	gai = GlobalAspectInfo.v();
    }

    public NodeVisitor enter(Node parent, Node n) {
	ParsedClassType scope = context().currentClassScope();
	if (scope != null) {
	    String clname = scope.fullName();
	    if (!clname.equals(current_aspect_name)) {
		current_aspect_name = clname;
		current_aspect = gai.getAspect(clname);
	    }
	}
	if (n instanceof ContainsAspectInfo) {
	    ((ContainsAspectInfo)n).update(gai, current_aspect);
	}
	//System.out.println(n.getClass());
	return super.enter(parent, n);
    }

    public static AbcType toAbcType(polyglot.types.Type t) {
	return new AbcType(soot.javaToJimple.Util.getSootType(t));
    }

    public static int convertModifiers(Flags flags) {
	//TODO
	return 0;
    }

    public static MethodSig makeMethodSig(MethodDecl md) {
	ReferenceType mcc = md.methodInstance().container();
	if (!(mcc instanceof ParsedClassType)) {
	    throw new RuntimeException("Error in aspect info generation: Method is not in a named class");
	}
	int mod = convertModifiers(md.flags());
	AbcClass cl = GlobalAspectInfo.v().getClass(((ParsedClassType)mcc).fullName());
	AbcType rtype = toAbcType(md.returnType().type());
	String name = md.name();
	List formals = new ArrayList();
	Iterator mdfi = md.formals().iterator();
	while (mdfi.hasNext()) {
	    polyglot.ast.Formal mdf = (polyglot.ast.Formal)mdfi.next();
	    formals.add(new abc.weaving.aspectinfo.Formal(toAbcType((polyglot.types.Type)mdf.type().type()),
							  mdf.name(), mdf.position()));
	}
	List exc = new ArrayList();
	Iterator ti = md.throwTypes().iterator();
	while (ti.hasNext()) {
	    TypeNode t = (TypeNode)ti.next();
	    exc.add(t.type().toString());
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, md.position());
    }


}

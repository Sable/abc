
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

public class AbcFactory {
    public static AbcType AbcType(polyglot.types.Type t) {
	return new AbcType(t);
    }

    public static AbcType AbcType(soot.Type t) {
	return new AbcType(t);
    }

    public static AbcClass AbcClass(polyglot.types.ClassType ct) {
	return new AbcClass(ct);
    }

    public static AbcClass AbcClass(polyglot.types.ClassType ct, String java_name) {
	return new AbcClass(ct, java_name);
    }

    public static AbcClass AbcClass(soot.SootClass sc) {
	return new AbcClass(sc);
    }

    public static MethodSig MethodSig(polyglot.ast.MethodDecl m) {
	return MethodSig(m, (polyglot.types.ClassType)m.methodInstance().container());
    }

    public static MethodSig MethodSig(polyglot.ast.MethodDecl m, polyglot.types.ClassType container) {
	int mod = soot.javaToJimple.Util.getModifier(m.flags());
	AbcClass cl = new AbcClass(container);
	AbcType rtype = new AbcType(m.returnType().type());
	String name = m.name();
	List formals = new ArrayList();
	Iterator mdfi = m.formals().iterator();
	while (mdfi.hasNext()) {
	    polyglot.ast.Formal mdf = (polyglot.ast.Formal)mdfi.next();
	    formals.add(new abc.weaving.aspectinfo.Formal(new AbcType((polyglot.types.Type)mdf.type().type()),
							  mdf.name(), mdf.position()));
	}
	List exc = new ArrayList();
	Iterator ti = m.throwTypes().iterator();
	while (ti.hasNext()) {
	    polyglot.ast.TypeNode t = (polyglot.ast.TypeNode)ti.next();
	    exc.add(t.type().toString());
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, m.position());
    }

    public static MethodSig MethodSig(soot.SootMethod m) {
	int mod = m.getModifiers();
	AbcClass cl = new AbcClass(m.getDeclaringClass());
	AbcType rtype = new AbcType(m.getReturnType());
	String name = m.getName();
	List formals = new ArrayList();
	Iterator mfti = m.getParameterTypes().iterator();
	while (mfti.hasNext()) {
	    soot.Type mft = (soot.Type)mfti.next();
	    formals.add(new Formal(new AbcType(mft), "dummy_name", null));
	}
	List exc = new ArrayList();
	Iterator ti = m.getExceptions().iterator();
	while (ti.hasNext()) {
	    soot.SootClass t = (soot.SootClass)ti.next();
	    exc.add(t.toString());
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, null);
    }


}

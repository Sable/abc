
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

    public static int modifiers(polyglot.types.Flags mods) {
	return soot.javaToJimple.Util.getModifier(mods);
    }

    public static MethodSig MethodSig(polyglot.ast.MethodDecl m) {
	return MethodSig(m, (polyglot.types.ClassType)m.methodInstance().container());
    }

    public static MethodSig MethodSig(polyglot.ast.MethodDecl m, polyglot.types.ClassType container) {
	int mod = soot.javaToJimple.Util.getModifier(m.flags());
	AbcClass cl = AbcFactory.AbcClass(container);
	AbcType rtype = AbcFactory.AbcType(m.returnType().type());
	String name = m.name();
	List formals = new ArrayList();
	Iterator mdfi = m.formals().iterator();
	while (mdfi.hasNext()) {
	    polyglot.ast.Formal mdf = (polyglot.ast.Formal)mdfi.next();
	    formals.add(new abc.weaving.aspectinfo.Formal(AbcFactory.AbcType((polyglot.types.Type)mdf.type().type()),
							  mdf.name(), mdf.position()));
	}
	List exc = new ArrayList();
	Iterator ti = m.throwTypes().iterator();
	while (ti.hasNext()) {
	    polyglot.ast.TypeNode t = (polyglot.ast.TypeNode)ti.next();
	    exc.add(AbcFactory.AbcClass((polyglot.types.ClassType)t.type()));
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, m.position());
    }

    public static MethodSig MethodSig(polyglot.types.MethodInstance mi) {
	int mod = soot.javaToJimple.Util.getModifier(mi.flags());
	AbcClass cl = AbcFactory.AbcClass((polyglot.types.ClassType)mi.container());
	AbcType rtype = AbcFactory.AbcType(mi.returnType());
	String name = mi.name();
	List formals = new ArrayList();
	int index = 0;
	Iterator fi = mi.formalTypes().iterator(); 
	while (fi.hasNext()) {
	    polyglot.types.Type ft = (polyglot.types.Type)fi.next();
	    formals.add(new abc.weaving.aspectinfo.Formal(AbcFactory.AbcType(ft),"a$"+index, mi.position()));
	    index++;
	}
	List exc = new ArrayList();
	Iterator ti = mi.throwTypes().iterator();
	while (ti.hasNext()) {
	    polyglot.types.ClassType t = (polyglot.types.ClassType)ti.next();
	    exc.add(AbcFactory.AbcClass(t));
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, mi.position());	
    }

    public static MethodSig MethodSig(soot.SootMethod m) {
	int mod = m.getModifiers();
	AbcClass cl = AbcFactory.AbcClass(m.getDeclaringClass());
	AbcType rtype = AbcFactory.AbcType(m.getReturnType());
	String name = m.getName();
	List formals = new ArrayList();
	int index = 0;
	Iterator mfti = m.getParameterTypes().iterator();
	while (mfti.hasNext()) {
	    soot.Type mft = (soot.Type)mfti.next();
	    formals.add(new Formal(AbcFactory.AbcType(mft), "a$"+index, null));
	    index++;
	}
	List exc = new ArrayList();
	Iterator ti = m.getExceptions().iterator();
	while (ti.hasNext()) {
	    soot.SootClass t = (soot.SootClass)ti.next();
	    exc.add(AbcFactory.AbcClass(t));
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, null);
    }

    public static FieldSig FieldSig(polyglot.ast.FieldDecl f) {
	return FieldSig(f, (polyglot.types.ClassType)f.fieldInstance().container());
    }

    public static FieldSig FieldSig(polyglot.ast.FieldDecl f, polyglot.types.ClassType container) {
	int mod = soot.javaToJimple.Util.getModifier(f.flags());
	AbcClass cl = AbcFactory.AbcClass(container);
	AbcType type = AbcFactory.AbcType(f.type().type());
	String name = f.name();
	return new FieldSig(mod, cl, type, name, f.position());
    }

    public static FieldSig FieldSig(polyglot.types.FieldInstance fi) {
	int mod = soot.javaToJimple.Util.getModifier(fi.flags());
	AbcClass cl = AbcFactory.AbcClass((polyglot.types.ClassType)fi.container());
	AbcType type = AbcFactory.AbcType(fi.type());
	String name = fi.name();
	return new FieldSig(mod, cl, type, name, fi.position());
    }
 
    public static FieldSig FieldSig(soot.SootField f) {
	int mod = f.getModifiers();
	AbcClass cl = AbcFactory.AbcClass(f.getDeclaringClass());
	AbcType type = AbcFactory.AbcType(f.getType());
	String name = f.getName();
	return new FieldSig(mod, cl, type, name, null);
    }
}

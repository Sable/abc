/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.aspectinfo;

import polyglot.util.Position;
import polyglot.types.Resolver;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;

import soot.SootClass;
import soot.SootResolver;

import java.util.*;

/** 
 *  @author Aske Simon Christensen
 */

public class AbcFactory {
    private static Resolver resolver;

    private static Map/*<String,ClassType>*/ name_to_ct;
    private static Map/*<ClassType,String>*/ ct_to_name;


    public static void init(Resolver res) {
	resolver = res;
	name_to_ct = new HashMap();
	ct_to_name = new HashMap();
    }

    public static void reset() {
	resolver = null;
	name_to_ct = null;
	ct_to_name = null;
    }

    public static AbcType AbcType(polyglot.types.Type t) {
	return new AbcType(t);
    }

    public static AbcType AbcType(soot.Type t) {
	return new AbcType(t);
    }

    public static AbcClass AbcClass(ClassType ct) {
	return new AbcClass(ct);
    }

    public static AbcClass AbcClass(ClassType ct, String java_name) {
	return new AbcClass(ct, java_name);
    }

    public static AbcClass AbcClass(SootClass sc) {
	return new AbcClass(sc);
    }

    public static int modifiers(polyglot.types.Flags mods) {
	return soot.javaToJimple.Util.getModifier(mods);
    }

    public static ClassType sootClassToClassType(SootClass sc) {
	boolean debug = abc.main.Debug.v().sootClassToClassType;
	if (debug) System.err.print("To ClassType: "+sc.getName()+" ... ");
	if (name_to_ct.containsKey(sc.getName())) {
	    if (debug) System.err.println("KNOWN");
	    return (ClassType)name_to_ct.get(sc.getName());
	} else {
	    try {
		if (debug) System.err.println("LOOKUP");
		ClassType ct = (ClassType)resolver.find(sc.getName());
		name_to_ct.put(sc.getName(), ct);
		ct_to_name.put(ct, sc.getName());
		return ct;
	    } catch (SemanticException e) {
		throw (NoSuchElementException)new NoSuchElementException("No such class: "+sc).initCause(e);
	    }
	}
    }

    public static SootClass classTypeToSootClass(ClassType ct) {
	if (ct_to_name.containsKey(ct)) {
	    return SootResolver.v().makeClassRef((String)ct_to_name.get(ct));
	} else {
	    soot.RefType rt=(soot.RefType) soot.javaToJimple.Util.getSootType(ct);
	    SootClass sc = rt.getSootClass();
	    if(sc==null) 
		throw new polyglot.util.InternalCompilerError("Failed to get soot class of "+ct);
	    name_to_ct.put(sc.getName(), ct);
	    ct_to_name.put(ct, sc.getName());
	    return sc;
	}
    }

    public static void registerName(ClassType ct, String name) {
	name_to_ct.put(name, ct);
	ct_to_name.put(ct, name);
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
    	polyglot.types.Type retType = mi.returnType();
    	String methodname = mi.name();
	int mod = soot.javaToJimple.Util.getModifier(mi.flags());
	AbcClass cl = AbcFactory.AbcClass((polyglot.types.ClassType)mi.container());
	AbcType rtype = AbcFactory.AbcType(retType);
	String name = methodname;
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
    
	public static MethodSig MethodSig(polyglot.types.ConstructorInstance mi) {
	  int mod = soot.javaToJimple.Util.getModifier(mi.flags());
	  AbcClass cl = AbcFactory.AbcClass((polyglot.types.ClassType)mi.container());
	  AbcType rtype = AbcFactory.AbcType(mi.container());
	  String name = "<init>";
	  List formals = new ArrayList();
	  ClassType cont = mi.container().toClass();
	  if (cont.isInnerClass()) {
		formals.add(0,new abc.weaving.aspectinfo.Formal(AbcFactory.AbcType(cont.outer()),"outer$",mi.position()));
	  }
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
	    if(t==null) throw new polyglot.util.InternalCompilerError
			    ("Exception in throws list of soot method "+m+" was null");
	    exc.add(AbcFactory.AbcClass(t));
	}
	return new MethodSig(mod, cl, rtype, name, formals, exc, null);
    }

    public static MethodSig MethodSig(soot.SootMethodRef mr) {
	int mod = 0;
	AbcClass cl = AbcFactory.AbcClass(mr.declaringClass());
	AbcType rtype = AbcFactory.AbcType(mr.returnType());
	String name = mr.name();
	List formals = new ArrayList();
	int index = 0;
	Iterator mfti = mr.parameterTypes().iterator();
	while (mfti.hasNext()) {
	    soot.Type mft = (soot.Type)mfti.next();
	    formals.add(new Formal(AbcFactory.AbcType(mft), "a$"+index, null));
	    index++;
	}
	List exc = new ArrayList();
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

    public static FieldSig FieldSig(soot.SootFieldRef fr) {
	int mod = 0;
	AbcClass cl = AbcFactory.AbcClass(fr.declaringClass());
	AbcType type = AbcFactory.AbcType(fr.type());
	String name = fr.name();
	return new FieldSig(mod, cl, type, name, null);
    }
}


package abc.weaving.aspectinfo;

import soot.SootMethod;
import soot.Type;

import polyglot.ast.MethodDecl;
import polyglot.types.ParsedClassType;

import java.util.*;

public class MethodCategory {

    // CATEGORY DEFINITIONS

    /** A normal method */
    public static final int NORMAL = 0;

    /** A special aspect method, i.e. <code>aspectOf</code>... */
    // Generated in abc/aspectj/ast/AspectDecl_c.java
    public static final int ASPECT_INSTANCE = 1;

    /** An advice body */
    // Generated in abc/aspectj/ast/AdviceDecl_c.java
    public static final int ADVICE_BODY = 2;

    /** The expression in an <code>if</code> pointcut */
    // Generated in abc/aspectj/ast/PCIf_c.java
    public static final int IF_EXPR = 3;

    // **********

    /** The implementation placeholder of an intertype method declaration */
    // Generated in abc/aspectj/ast/IntertypeMethodDecl_c.java
    public static final int INTERTYPE_METHOD_SOURCE = 4;

    /** A woven intertype method declaration, delegating to the actual implementation */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_METHOD_DELEGATOR = 5;

    // **********

    /** The body of an intertype constructor declaration, without field initializers */
    // Generated in abc/aspectj/ast/IntertypeConstructorDecl_c.java
    public static final int INTERTYPE_CONSTRUCTOR_BODY = 6;

    /** The encapsulation of an argument to a <code>this</code> or <code>super</code> call
     *  in an intertype constructor declaration */
    // Generated in abc/aspectj/ast/IntertypeConstructorDecl_c.java
    public static final int INTERTYPE_CONSTRUCTOR_SPECIAL_ARG = 7;

    /** A woven intertype constructor declaration, delegating to the actual implementation */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_CONSTRUCTOR_DELEGATOR = 8;

    // **********

    /** The initializer for an intertype field declaration */
    // Generated in abc/aspectj/ast/IntertypeFieldDecl_c.java
    public static final int INTERTYPE_FIELD_INITIALIZER = 9;

    /** A method delegating a <code>this</code> or <code>super</code> call from an
     *  intertype method or constructor */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_SPECIAL_CALL_DELEGATOR = 10;

    // **********

    /** An accessor method to get the value of a field */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int ACCESSOR_GET = 11;
    /** An accessor method to set the value of a field */
    public static final int ACCESSOR_SET = 12;


    // CATEGORY PROPERTY TABLES

    // normal, aspect, asvice, if,
    // it_m_src, it_m_del,
    // it_c_body, it_c_arg, it_c_del,
    // it_f_init, it_spec_del,
    // acc_get, acc_set

    private static boolean[] weave_inside =
    {
	true, false, true, false/*AJC doesn't, but why not?*/,
	true, false,
	true, true, false,
	true, false,
	true/*?*/, true/*?*/
    };

    private static boolean[] weave_execution =
    {
	true, false, true, false,
	true, false,
	true, false, false,
	false, false,
	false/*?*/, false/*?*/
    };


    // CATEGORY PROPERTY QUERY METHODS

    public static boolean weaveInside(int cat) {
	return weave_inside[cat];
    }

    public static boolean weaveInside(SootMethod m)
    { return weaveInside(getCategory(m)); }
    public static boolean weaveInside(MethodSig m)
    { return weaveInside(getCategory(m)); }


    public static boolean weaveExecution(int cat) {
	return weave_execution[cat];
    }

    public static boolean weaveExecution(SootMethod m)
    { return weaveExecution(getCategory(m)); }
    public static boolean weaveExecution(MethodSig m)
    { return weaveExecution(getCategory(m)); }


    public static boolean adviceBody(int cat) {
	return cat == ADVICE_BODY;
    }

    public static boolean adviceBody(SootMethod m)
    { return adviceBody(getCategory(m)); }
    public static boolean adviceBody(MethodSig m)
    { return adviceBody(getCategory(m)); }

    

    // CATEGORY QUERY

    public static int getCategory(SootMethod m) {
	return GlobalAspectInfo.v().getMethodCategory(signature(m));
    }

    public static int getCategory(MethodSig m) {
	return GlobalAspectInfo.v().getMethodCategory(signature(m));
    }

    // REGISTRATION METHODS

    public static void register(SootMethod m, int cat) {
	GlobalAspectInfo.v().registerMethodCategory(signature(m), cat);
    }

    public static void register(MethodDecl m, int cat) {
	try {
	    GlobalAspectInfo.v().registerMethodCategory(signature(m, (ParsedClassType)m.methodInstance().container()), cat);
	} catch (ClassCastException e) {
	    throw new RuntimeException("Tried to register category of method "+m.name()+" in unnamed class");
	}
    }

    public static void register(MethodDecl m, ParsedClassType container, int cat) {
	GlobalAspectInfo.v().registerMethodCategory(signature(m, container), cat);
    }

    public static void register(MethodSig m, int cat) {
	GlobalAspectInfo.v().registerMethodCategory(signature(m), cat);
    }

    public static void register(String sig, int cat) {
	GlobalAspectInfo.v().registerMethodCategory(sig, cat);
    }

    private static String signature(SootMethod m) {
	StringBuffer sb = new StringBuffer();
	sb.append(d2d(m.getReturnType()));
	sb.append(" ");
	sb.append(d2d(m.getDeclaringClass()));
	sb.append(".");
	sb.append(m.getName());
	sb.append("(");
	Iterator pti = m.getParameterTypes().iterator();
	while (pti.hasNext()) {
	    Type pt = (Type)pti.next();
	    sb.append(d2d(pt));
	    if (pti.hasNext()) {
		sb.append(",");
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    private static String signature(MethodDecl m, ParsedClassType container) {
	StringBuffer sb = new StringBuffer();
	sb.append(d2d(m.returnType()));
	sb.append(" ");
	sb.append(d2d(container.fullName()));
	sb.append(".");
	sb.append(m.name());
	sb.append("(");
	Iterator fi = m.formals().iterator();
	while (fi.hasNext()) {
	    polyglot.ast.Formal f = (polyglot.ast.Formal)fi.next();
	    sb.append(d2d(f.type()));
	    if (fi.hasNext()) {
		sb.append(",");
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    private static String signature(MethodSig m) {
	StringBuffer sb = new StringBuffer();
	sb.append(d2d(m.getReturnType()));
	sb.append(" ");
	sb.append(d2d(m.getDeclaringClass()));
	sb.append(".");
	sb.append(m.getName());
	sb.append("(");
	Iterator fi = m.getFormals().iterator();
	while (fi.hasNext()) {
	    Formal f = (Formal)fi.next();
	    sb.append(d2d(f.getType()));
	    if (fi.hasNext()) {
		sb.append(",");
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    private static String d2d(Object type) {
	return type.toString().replace('$','.');
    }


}

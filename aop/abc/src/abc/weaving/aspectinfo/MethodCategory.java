
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

    /** A <code>proceed</code> dummy method */
    public static final int PROCEED = 3;

    /** The expression in an <code>if</code> pointcut */
    // Generated in abc/aspectj/ast/PCIf_c.java
    public static final int IF_EXPR = 4;

    // **********

    /** The implementation placeholder of an intertype method declaration.
     *  This will have its name as real name and the host class as real class. */
    // Generated in abc/aspectj/ast/IntertypeMethodDecl_c.java
    public static final int INTERTYPE_METHOD_SOURCE = 5;

    /** A woven intertype method declaration, delegating to the actual implementation.
     *  This will have its name as real name and the host class as real class. */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_METHOD_DELEGATOR = 6;

    // **********

    /** The body of an intertype constructor declaration, without field initializers.
     *  This will have real name <code>&lt;init&gt;</code> and the host class as real class. */
    // Generated in abc/aspectj/ast/IntertypeConstructorDecl_c.java
    public static final int INTERTYPE_CONSTRUCTOR_BODY = 7;

    /** The encapsulation of an argument to a <code>this</code> or <code>super</code> call
     *  in an intertype constructor declaration */
    // Generated in abc/aspectj/ast/IntertypeConstructorDecl_c.java
    public static final int INTERTYPE_CONSTRUCTOR_SPECIAL_ARG = 8;

    /** A woven intertype constructor declaration, delegating to the actual implementation */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_CONSTRUCTOR_DELEGATOR = 9;

    // **********

    /** The initializer for an intertype field declaration */
    // Generated in abc/aspectj/ast/IntertypeFieldDecl_c.java
    public static final int INTERTYPE_FIELD_INITIALIZER = 10;

    /** A method delegating a <code>this</code> or <code>super</code> call from an
     *  intertype method or constructor */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_SPECIAL_CALL_DELEGATOR = 11;

    // **********

    /** An accessor method to get the value of a field.
     *  This will have the name and class of the field as real name class. */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int ACCESSOR_GET = 12;
    /** An accessor method to set the value of a field.
     *  This will have the name and class of the field as real name class. */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int ACCESSOR_SET = 13;


    // CATEGORY PROPERTY TABLES

    // normal, aspect, advice, proceed, if,
    // it_m_src, it_m_del,
    // it_c_body, it_c_arg, it_c_del,
    // it_f_init, it_spec_del,
    // acc_get, acc_set

    private static final boolean[] weave_inside =
    {
	true, false, true, false, false/*AJC doesn't, but why not?*/,
	true, false,
	true, true, false,
	true, false,
	true/*?*/, true/*?*/
    };

    private static final boolean[] weave_execution =
    {
	true, false, true, false, false,
	true, false,
	true, false, false,
	false, false,
	false/*?*/, false/*?*/
    };

    private static final boolean[] weave_calls =
    {
	true, true, false, false, false,
	false, true,
	false, false, true,
	false, true/*?*/,
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


    public static boolean weaveCalls(int cat) {
	return weave_calls[cat];
    }

    public static boolean weaveCalls(SootMethod m)
    { return weaveCalls(getCategory(m)); }
    public static boolean weaveCalls(MethodSig m)
    { return weaveCalls(getCategory(m)); }


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

    public static void register(String sig, int cat) {
	GlobalAspectInfo.v().registerMethodCategory(sig, cat);
    }

    public static void register(SootMethod m, int cat) {
	register(signature(m), cat);
    }

    public static void register(MethodDecl m, int cat) {
	try {
	    register(signature(m, (ParsedClassType)m.methodInstance().container()), cat);
	} catch (ClassCastException e) {
	    throw new RuntimeException("Tried to register category of method "+m.name()+" in unnamed class");
	}
    }

    public static void register(MethodDecl m, ParsedClassType container, int cat) {
	register(signature(m, container), cat);
    }

    public static void register(MethodSig m, int cat) {
	register(signature(m), cat);
    }

    // REAL NAME REGISTRATION

    public static void registerRealNameAndClass(String sig, String real_name, String real_class) {
	GlobalAspectInfo.v().registerRealNameAndClass(sig, real_name, d2d(real_class));
    }

    public static void registerRealNameAndClass(SootMethod m, String real_name, String real_class) {
	registerRealNameAndClass(signature(m), real_name, real_class);
    }

    public static void registerRealNameAndClass(MethodDecl m, String real_name, String real_class) {
	try {
	    registerRealNameAndClass(signature(m, (ParsedClassType)m.methodInstance().container()), real_name, real_class);
	} catch (ClassCastException e) {
	    throw new RuntimeException("Tried to register name and class of method "+m.name()+" in unnamed class");
	}
    }

    public static void registerRealNameAndClass(MethodDecl m, ParsedClassType container, String real_name, String real_class) {
	registerRealNameAndClass(signature(m, container), real_name, real_class);
    }

    public static void registerRealNameAndClass(MethodSig m, String real_name, String real_class) {
	registerRealNameAndClass(signature(m), real_name, real_class);
    }

    // REAL NAME QUERY

    public static String getName(SootMethod m) {
	String real_name = GlobalAspectInfo.v().getRealName(signature(m));
	if (real_name == null) {
	    return m.getName().toString();
	} else {
	    return real_name;
	}
    }

    public static String getName(MethodSig m) {
	String real_name = GlobalAspectInfo.v().getRealName(signature(m));
	if (real_name == null) {
	    return m.getName().toString();
	} else {
	    return real_name;
	}
    }

    public static String getClassName(SootMethod m) {
	String real_class = GlobalAspectInfo.v().getRealClass(signature(m));
	if (real_class == null) {
	    return d2d(m.getDeclaringClass().getName());
	} else {
	    return real_class;
	}
    }

    public static String getClassName(MethodSig m) {
	String real_class = GlobalAspectInfo.v().getRealClass(signature(m));
	if (real_class == null) {
	    return d2d(m.getDeclaringClass().getName());
	} else {
	    return real_class;
	}
    }

    // SIGNATURE CALCULATION METHODS

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


package abc.weaving.aspectinfo;

import polyglot.util.InternalCompilerError;

import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Type;

import polyglot.ast.MethodDecl;
import polyglot.types.ClassType;
import polyglot.types.Flags;

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
    
    /** The delegate for an initializer for an intertype field declaration */
    public static final int INTERTYPE_INITIALIZER_DELEGATE = 11;

    /** A method delegating a <code>this</code> or <code>super</code> call from an
     *  intertype method or constructor */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int INTERTYPE_SPECIAL_CALL_DELEGATOR = 12;

    // **********

    /** An accessor method to get the value of a field.
     *  This will have the name and class of the field as real name class. */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int ACCESSOR_GET = 13;
    /** An accessor method to set the value of a field.
     *  This will have the name and class of the field as real name class. */
    // Generated in abc/weaving/weaver/IntertypeAdjuster.java
    public static final int ACCESSOR_SET = 14;


	// ******
	/** An accessor method to get the value of a qualified use of "this" or "super"
	 * inside an intertype method.
	 */
	// Generated in abc/weaving/weaver/IntertypeAdjuster.java
	public static final int THIS_GET = 15;
	
    // CATEGORY PROPERTY TABLES

    // normal, aspect, advice, proceed, if,
    // it_m_src, it_m_del,
    // it_c_body, it_c_arg, it_c_del,
    // it_f_init, it_f_deleg, it_spec_del,
    // acc_get, acc_set, this_get

    private static final boolean[] weave_inside =
    {
	true, false, true, false, false/*AJC doesn't, but why not?*/,
	true, false,
	true, true, true,
	true, true, false,
	false, false, false
    };

    private static final boolean[] weave_execution =
    {
	true, false, true, false, false,
	true, false,
	true, false, true,
	false, true, false,
	false/*?*/, false/*?*/, false
    };

    private static final boolean[] weave_calls =
    {
	true, true, false, false, false,
	false, true,
	false, false, true,
	false, true, true/*?*/,
	false/*?*/, false/*?*/, false
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
	return GlobalAspectInfo.v().getMethodCategory(AbcFactory.MethodSig(m));
    }

    public static int getCategory(MethodSig m) {
	return GlobalAspectInfo.v().getMethodCategory(m);
    }

    // REGISTRATION METHODS

    public static void register(MethodSig sig, int cat) {
	GlobalAspectInfo.v().registerMethodCategory(sig, cat);
    }

    public static void register(SootMethod m, int cat) {
	register(AbcFactory.MethodSig(m), cat);
    }

    public static void register(MethodDecl m, int cat) {
	register(AbcFactory.MethodSig(m), cat);
    }

    public static void register(MethodDecl m, ClassType container, int cat) {
	register(AbcFactory.MethodSig(m, container), cat);
    }

    // REAL NAME REGISTRATION

    public static void registerRealNameAndClass(MethodSig sig,
						int mods,
						String real_name, AbcClass real_class,
						int skip_first, int skip_last) {
	GlobalAspectInfo.v().registerRealNameAndClass(sig,
						      mods,
						      real_name, real_class,
						      skip_first, skip_last);
    }

    public static void registerRealNameAndClass(SootMethod m,
						int mods,
						String real_name, AbcClass real_class,
						int skip_first, int skip_last) {
	registerRealNameAndClass(AbcFactory.MethodSig(m),
				 mods,
				 real_name, real_class,
				 skip_first, skip_last);
    }

    public static void registerRealNameAndClass(MethodDecl m,
						Flags mods,
						String real_name, AbcClass real_class,
						int skip_first, int skip_last) {
	registerRealNameAndClass(AbcFactory.MethodSig(m),
				 AbcFactory.modifiers(mods),
				 real_name, real_class,
				 skip_first, skip_last);
    }

    public static void registerRealNameAndClass(MethodDecl m, ClassType container,
						Flags mods,
						String real_name, AbcClass real_class,
						int skip_first, int skip_last) {
	registerRealNameAndClass(AbcFactory.MethodSig(m, container),
				 AbcFactory.modifiers(mods),
				 real_name, real_class,
				 skip_first, skip_last);
    }
    
	public static void registerRealNameAndClass(FieldSig sig,
						   int mods,
						   String real_name, AbcClass real_class) {
	   GlobalAspectInfo.v().registerRealNameAndClass(sig,
								 mods,
								 real_name, real_class);
	}

   	public static void registerRealNameAndClass(SootField m,
					   int mods,
					   String real_name, AbcClass real_class) {
   	registerRealNameAndClass(AbcFactory.FieldSig(m),
				mods,
				real_name, real_class);
   	}

    // REAL NAME QUERY

    public static int getModifiers(SootMethod m) {
	return GlobalAspectInfo.v().getRealModifiers(AbcFactory.MethodSig(m), m.getModifiers());
    }

    public static int getModifiers(MethodSig m) {
	return GlobalAspectInfo.v().getRealModifiers(m, m.getModifiers());
    }

    public static String getName(SootMethod m) {
	String real_name = GlobalAspectInfo.v().getRealName(AbcFactory.MethodSig(m));
	if (real_name == null) {
	    return m.getName().toString();
	} else {
	    return real_name;
	}
    }

    public static String getName(MethodSig m) {
	String real_name = GlobalAspectInfo.v().getRealName(m);
	if (real_name == null) {
	    return m.getName().toString();
	} else {
	    return real_name;
	}
    }

    public static SootClass getClass(SootMethod m) {
	AbcClass real_class = GlobalAspectInfo.v().getRealClass(AbcFactory.MethodSig(m));
	if (real_class == null) {
	    return m.getDeclaringClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static SootClass getClass(MethodSig m) {
	AbcClass real_class = GlobalAspectInfo.v().getRealClass(m);
	if (real_class == null) {
	    return m.getDeclaringClass().getSootClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static int getSkipFirst(SootMethod m) {
	return GlobalAspectInfo.v().getSkipFirst(AbcFactory.MethodSig(m));
    }

    public static int getSkipLast(SootMethod m) {
	return GlobalAspectInfo.v().getSkipLast(AbcFactory.MethodSig(m));
    }
    
	public static int getModifiers(SootField m) {
	   return GlobalAspectInfo.v().getRealModifiers(AbcFactory.FieldSig(m), m.getModifiers());
	}

	public static int getModifiers(FieldSig m) {
	   return GlobalAspectInfo.v().getRealModifiers(m, m.getModifiers());
	}

	public static String getName(SootField m) {
	   FieldSig fs = AbcFactory.FieldSig(m);
	   String real_name = GlobalAspectInfo.v().getRealName(AbcFactory.FieldSig(m));
	   if (real_name == null) {
		   return m.getName().toString();
	   } else {
		   return real_name;
	   }
	}

	public static String getName(FieldSig m) {
	   String real_name = GlobalAspectInfo.v().getRealName(m);
	   if (real_name == null) {
		   return m.getName().toString();
	   } else {
		   return real_name;
	   }
	}

	public static SootClass getClass(SootField m) {
	   AbcClass real_class = GlobalAspectInfo.v().getRealClass(AbcFactory.FieldSig(m));
	   if (real_class == null) {
		   return m.getDeclaringClass();
	   } else {
		   return real_class.getSootClass();
	   }
	}

	public static SootClass getClass(FieldSig m) {
	   AbcClass real_class = GlobalAspectInfo.v().getRealClass(m);
	   if (real_class == null) {
		   return m.getDeclaringClass().getSootClass();
	   } else {
		   return real_class.getSootClass();
	   }
	}
	
	public static SootField getField(SootMethod sm) {
		FieldSig fs = GlobalAspectInfo.v().getField(AbcFactory.MethodSig(sm));
		if (fs == null) {
			throw new InternalCompilerError("get field on a method that is not an accessor");
		} else {
			return fs.getSootField();
		}
	}
	
	
	public static void registerFieldGet(FieldSig fs, MethodSig sig) {
	   GlobalAspectInfo.v().registerMethodCategory(sig, MethodCategory.ACCESSOR_GET);
	   GlobalAspectInfo.v().registerFieldAccessor(fs,sig);
	}

	public static void registerFieldSet(FieldSig fs, MethodSig sig) {
		GlobalAspectInfo.v().registerMethodCategory(sig, MethodCategory.ACCESSOR_SET);
		GlobalAspectInfo.v().registerFieldAccessor(fs,sig);
	}

	public static void registerFieldGet(SootField sfs, SootMethod ssig) {
		FieldSig fs = AbcFactory.FieldSig(sfs);
		MethodSig sig = AbcFactory.MethodSig(ssig);
		registerFieldGet(fs,sig);
	}

	public static void registerFieldSet(SootField sfs, SootMethod ssig) {
		FieldSig fs = AbcFactory.FieldSig(sfs);
		MethodSig sig = AbcFactory.MethodSig(ssig);
		registerFieldSet(fs,sig);
	}
	
    /** is this an ITD (method or field initialiser) that has "this"
     *  as a parameter?
     */
    public static boolean hasThisAsFirstParameter(SootMethod m) {
    	return (getCategory(m) == INTERTYPE_FIELD_INITIALIZER ||
    			getCategory(m) == INTERTYPE_INITIALIZER_DELEGATE ||
    	        getCategory(m) == INTERTYPE_METHOD_SOURCE) &&
    	        getSkipFirst(m) == 1; // FIXME: this is a fragile test to
    	                              // see whether the ITM was declared static
    }

}

/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Oege de Moor
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

import polyglot.util.InternalCompilerError;

import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SootField;
import soot.SootFieldRef;
import soot.Type;
import soot.tagkit.Host;

import polyglot.ast.MethodDecl;
import polyglot.types.ClassType;
import polyglot.types.Flags;

import java.util.*;

/** Decide what a field or method in jimple really is
 *  @author Aske Simon Christensen
 *  @author Oege de Moor
 */
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

    public static boolean weaveCalls(SootMethodRef mr)
    { return weaveCalls(mr.resolve()); }

    public static boolean weaveCalls(SootMethod m)
    {
        if (m.hasTag("SyntheticTag"))
            return false;
        return weaveCalls(getCategory(m));
    }

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
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getMethodCategory(AbcFactory.MethodSig(m));
    }

    public static int getCategory(MethodSig m) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getMethodCategory(m);
    }

    public static int getCategory(SootMethodRef mr) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getMethodCategory(AbcFactory.MethodSig(mr));
    }

    // REGISTRATION METHODS

    public static void register(MethodSig sig, int cat) {
	abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerMethodCategory(sig, cat);
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
	abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerRealNameAndClass(sig,
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
	abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerRealNameAndClass(sig,
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
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealModifiers(AbcFactory.MethodSig(m), m.getModifiers());
    }

    public static int getModifiers(MethodSig m) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealModifiers(m, m.getModifiers());
    }

    // FIXME: temporary stub
    public static int getModifiers(SootMethodRef m) {
	return getModifiers(m.resolve());
    }

    public static String getName(SootMethod m) {
	String real_name = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(AbcFactory.MethodSig(m));
	if (real_name == null) {
	    return m.getName();
	} else {
	    return real_name;
	}
    }

    public static String getName(MethodSig m) {
	String real_name = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(m);
	if (real_name == null) {
	    return m.getName();
	} else {
	    return real_name;
	}
    }

    public static String getName(SootMethodRef mr) {
	String real_name = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(AbcFactory.MethodSig(mr));
	if (real_name == null) {
	    return mr.name();
	} else {
	    return real_name;
	}
    }

    public static SootClass getClass(SootMethod m) {
	AbcClass real_class = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealClass(AbcFactory.MethodSig(m));
	if (real_class == null) {
	    return m.getDeclaringClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static SootClass getClass(MethodSig m) {
	AbcClass real_class = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealClass(m);
	if (real_class == null) {
	    return m.getDeclaringClass().getSootClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static SootClass getClass(SootMethodRef mr) {
	AbcClass real_class = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealClass(AbcFactory.MethodSig(mr));
	if (real_class == null) {
	    return mr.declaringClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static int getSkipFirst(SootMethod m) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getSkipFirst(AbcFactory.MethodSig(m));
    }

    // FIXME: temporary stub
    public static int getSkipFirst(SootMethodRef m) {
	return getSkipFirst(m.resolve());
    }

    public static int getSkipLast(SootMethod m) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getSkipLast(AbcFactory.MethodSig(m));
    }

    // FIXME: temporary stub
    public static int getSkipLast(SootMethodRef m) {
	return getSkipLast(m.resolve());
    }

    public static int getModifiers(SootField m) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealModifiers(AbcFactory.FieldSig(m), m.getModifiers());
    }

    public static int getModifiers(FieldSig m) {
	return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealModifiers(m, m.getModifiers());
    }

    //FIXME: temporary stub
    public static int getModifiers(SootFieldRef f) {
	return getModifiers(f.resolve());
    }


    public static String getName(SootField f) {
	FieldSig fs = AbcFactory.FieldSig(f);
	String real_name = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(AbcFactory.FieldSig(f));
	if (real_name == null) {
	    return f.getName();
	} else {
	    return real_name;
	}
    }

    public static String getName(FieldSig f) {
	String real_name = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(f);
	if (real_name == null) {
	    return f.getName();
	} else {
	    return real_name;
	}
    }

    public static String getName(SootFieldRef fr) {
	FieldSig fs = AbcFactory.FieldSig(fr);
	String real_name = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(AbcFactory.FieldSig(fr));
	if (real_name == null) {
	    return fr.name();
	} else {
	    return real_name;
	}
    }

    public static SootClass getClass(SootField f) {
	AbcClass real_class = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealClass(AbcFactory.FieldSig(f));
	if (real_class == null) {
	    return f.getDeclaringClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static SootClass getClass(FieldSig f) {
	AbcClass real_class = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealClass(f);
	if (real_class == null) {
	    return f.getDeclaringClass().getSootClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static SootClass getClass(SootFieldRef fr) {
	AbcClass real_class = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealClass(AbcFactory.FieldSig(fr));
	if (real_class == null) {
	    return fr.declaringClass();
	} else {
	    return real_class.getSootClass();
	}
    }

    public static SootField getField(SootMethod sm) {
	FieldSig fs = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getField(AbcFactory.MethodSig(sm));
	if (fs == null) {
	    throw new InternalCompilerError("get field on a method that is not an accessor");
	} else {
	    return fs.getSootField();
	}
    }

    // FIXME: Temporary stub
    public static SootFieldRef getFieldRef(SootMethodRef smr) {
	return getField(smr.resolve()).makeRef();
    }
	
	public static void registerFieldGet(FieldSig fs, MethodSig sig) {
	   abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerMethodCategory(sig, MethodCategory.ACCESSOR_GET);
	   abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerFieldAccessor(fs,sig);
	}

	public static void registerFieldSet(FieldSig fs, MethodSig sig) {
		abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerMethodCategory(sig, MethodCategory.ACCESSOR_SET);
		abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerFieldAccessor(fs,sig);
	}

    // FIXME: Temporary stub
    public static void registerFieldGet(SootFieldRef sfsr, SootMethod ssig) {
	registerFieldGet(sfsr.resolve(),ssig);
    }

	public static void registerFieldGet(SootField sfs, SootMethod ssig) {
		FieldSig fs = AbcFactory.FieldSig(sfs);
		MethodSig sig = AbcFactory.MethodSig(ssig);
		registerFieldGet(fs,sig);
	}

    // FIXME: Temporary stub
    public static void registerFieldSet(SootFieldRef sfsr, SootMethod ssig) {
	registerFieldSet(sfsr.resolve(),ssig);
    }

	public static void registerFieldSet(SootField sfs, SootMethod ssig) {
		FieldSig fs = AbcFactory.FieldSig(sfs);
		MethodSig sig = AbcFactory.MethodSig(ssig);
		registerFieldSet(fs,sig);
	}
	
	/* don't weave if this is a synthetic field introduced by normal Java compilation.
	 */
	public static boolean weaveSetGet(SootField sfs) {
	    return !((abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getRealName(AbcFactory.FieldSig(sfs)) == null) &&
	    //		         (sfs.getName().indexOf('$') != -1));
		     sfs.hasTag("SyntheticTag"));
	}

    // FIXME: Temporary stub
    public static boolean weaveSetGet(SootFieldRef sfr) {
	return weaveSetGet(sfr.resolve());
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

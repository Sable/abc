/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Damien Sereni
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

import java.util.*;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** Handler for a pointcut reference. 
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 *  @author Damien Sereni
 */
public class PointcutRef extends Pointcut {
    private Object decl_key;
    private Map/*<Object,PointcutDecl>*/ decl_map;
    private List/*<ArgPattern>*/ args;
    private Map/*<Aspect,PointcutDecl>*/ decls = new HashMap();
    
    /** Create an <code>args</code> pointcut.
     *  @param decl_key an object that can later be resolved into the pointcut declaration.
     *  @param decl_map a map from {@link java.lang.Object} to {@link abc.weaving.aspectinfo.PointcutDecl}.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public PointcutRef(Object decl_key, Map decl_map, List args, Position pos) {
	super(pos);
	decl_map.size();
	this.decl_key = decl_key;
	this.decl_map = decl_map;
	this.args = args;
    }

    private PointcutDecl getDirectDecl() {
	return (PointcutDecl) decl_map.get(decl_key);
    }

    public PointcutDecl getDecl(Aspect context) {
	PointcutDecl decl = (PointcutDecl) decls.get(context);
	if (decl == null) {
	    decl = getDirectDecl();
	    if (decl.isAbstract()) {
		decl = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPointcutDecl(decl.getName(), context);
		if(decl.isAbstract()) 
		    throw new InternalCompilerError("decl for "+this+" in context "+context+" was abstract");
	    }
	    decls.put(context, decl);
	}
	return decl;
    }

    /** Get the list of argument patterns.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public List getArgs() {
	return args;
    }

    public String toString() {
	return getDirectDecl().getName()+"(...)";
    }

    public Residue matchesAt(MatchingContext mc) {
	throw new InternalCompilerError
	    ("PointcutRef should have been inlined by now",getPosition());
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context,
			      int cflowdepth) {
	Iterator actualsIt=args.iterator();
	Iterator formalsIt=getDecl(context).getFormals().iterator();

	List/*<Formal>*/ newLocals=new LinkedList();
	List/*<CastPointcutVar>*/ newCasts=new LinkedList();

	Hashtable/*<String,Var>*/ declRenameEnv=new Hashtable();
	Hashtable/*<String,Abctype>*/ declTypeEnv=new Hashtable();

	while(actualsIt.hasNext() || formalsIt.hasNext()) {
	    ArgPattern actual = (ArgPattern) actualsIt.next();
	    Formal formal = (Formal) formalsIt.next();

	    Var param=actual.substituteForPointcutFormal
		(renameEnv,typeEnv,formal,newLocals,newCasts,getPosition());

	    declTypeEnv.put(formal.getName(),formal.getType());
	    declRenameEnv.put(formal.getName(),param);
	}

	Pointcut pc;
	try {
	    PointcutDecl d=getDecl(context);
	    if(d.isAbstract()) 
		throw new InternalCompilerError("Got an abstract pointcut decl while inlining "+this);
	    pc=getDecl(context).getPointcut()
		.inline(declRenameEnv,declTypeEnv,context,cflowdepth);
	} catch(NullPointerException e) {
	    throw new InternalCompilerError("NPE while trying to inline "+this+" with context "+context,e);
	}

	Iterator castsIt=newCasts.iterator();
	while(castsIt.hasNext()) {
	    CastPointcutVar cpv=(CastPointcutVar) castsIt.next();
	    pc=AndPointcut.construct(pc,cpv,getPosition());
	}

	if(!newLocals.isEmpty()) {
	    pc=new LocalPointcutVars(pc,newLocals,getPosition());
	}

	return pc;

    }
    public void registerSetupAdvice(Aspect context,Hashtable typeMap) {
	throw new InternalCompilerError
	    ("PointcutRef should have been inlined by now",getPosition());
    }
    public void getFreeVars(Set s) {
	throw new InternalCompilerError
	    ("PointcutRef should have been inlined by now",getPosition());
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#unify(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable, java.util.Hashtable, abc.weaving.aspectinfo.Pointcut)
	 */
	public boolean unify(Pointcut otherpc, Unification unification) {
		throw new InternalCompilerError
			("PointcutRef should have been inlined by now (unify(pc,ren1,ren2,res)",
			 getPosition());
	}
}

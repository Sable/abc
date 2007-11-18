/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ast.AmbExpr;
import polyglot.ast.Block;
import polyglot.ast.ClassMember;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ext.jl.ast.FieldDecl_c;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AspectBody;
import abc.aspectj.ast.AspectDecl_c;
import abc.aspectj.ast.IsSingleton;
import abc.aspectj.ast.PerClause;
import abc.aspectj.visit.AJTypeBuilder;
import abc.ra.ExtensionInfo;
import abc.ra.types.RelAspectType;
import abc.ra.weaving.aspectinfo.RelationalAspect;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.MethodSig;
import abc.weaving.aspectinfo.Per;

/**
 * Declaration of a relational aspect.
 * This is similar to a normal aspect but has additional formals in its header.
 * Also, its per-claus is always <i>issingleton</i>.
 *
 * @author Eric Bodden
 */
public class RelAspectDecl_c extends AspectDecl_c implements RelAspectDecl {

	/** The relational aspect formals. */
	protected List<Formal> formals;
	
	/** Names of tracematch body methods. */
	protected ArrayList<String> tmBodyMethodNames;

	public RelAspectDecl_c(Position pos, boolean is_privileged, Flags flags,
			String name, TypeNode superClass, List interfaces, PerClause per, List formals,
			AspectBody body) {
		super(pos, is_privileged, flags, name, superClass, interfaces, per,
				body);		 
		if(!(per instanceof IsSingleton)) {
			throw new RuntimeException("Relational aspects must have 'issingleton' per-clause.");
		}
		this.formals = formals;
		this.tmBodyMethodNames = new ArrayList<String>();
	}

	/**
	 * {@inheritDoc}
	 */
	public RelAspectDecl declareMethods(NodeFactory nf, TypeSystem ts) {
		RelAspectDecl_c copy = reconstruct(formals);
		List classMembers = copy.body().members();
		List newClassMembers = new ArrayList(classMembers);
		// create the associate and release methods
		// void type node
		TypeNode vd = nf.CanonicalTypeNode(Position.compilerGenerated(), ts.Void()).type(ts.Void());
		// create block for associate method
		Block ascb = nf.Block(Position.compilerGenerated());
		Iterator i = formals.iterator();
		// associate and release take same formals as aspect but because there is no typechecking done yet
		// we must create ambiguous expressions
		List<AmbExpr> args = new LinkedList<AmbExpr>();
		while (i.hasNext()) {
			polyglot.ast.Formal f = (polyglot.ast.Formal) i.next();
			AmbExpr fd = (AmbExpr) nf.AmbExpr(Position.compilerGenerated(), f.name());
			args.add(fd);
		}
		ascb = ascb.append(nf.Return(Position.compilerGenerated(), nf.NullLit(Position.compilerGenerated()))); //return null; will be replaced by backend
		Block relb = nf.Block(Position.compilerGenerated());
		MethodDecl asc = nf.MethodDecl(Position.compilerGenerated(), Flags.PUBLIC.Static(), nf.AmbTypeNode(Position.compilerGenerated(), name()), "associate", formals, Collections.EMPTY_LIST, ascb);
		MethodDecl rel = nf.MethodDecl(Position.compilerGenerated(), Flags.PUBLIC.Static(), vd, "release", formals, Collections.EMPTY_LIST, relb);
		newClassMembers.add(asc);
		newClassMembers.add(rel);
		newClassMembers = TypedList.copyAndCheck(newClassMembers, ClassMember.class, true);
		return (RelAspectDecl) copy.body(copy.body().members(newClassMembers));
	}

	/**
	 * Flags the type node for this aspect as "relational".
	 */
	public Node buildTypes(TypeBuilder tb) throws SemanticException {
		Node resultNode = super.buildTypes(tb);

		AJTypeBuilder ajtb = (AJTypeBuilder) tb;
		RelAspectType ct = (RelAspectType) ajtb.currentClass();
		ct.relational(true);
		ct.relationalAspectFormals(formals);
		
		return resultNode;
	}
	
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		RelAspectType at = (RelAspectType) type();
		
		//check if the "relational" flag was really set
		if(!flags.contains(ExtensionInfo.RELATIONAL_MODIFIER)) {
			throw new SemanticException("Only relational aspects are allowed to declare formal parameters.",position());
		}
		
//		//check if this class declares a field with the same name as a formal
//		for (Iterator<String> fieldNameIter = duplicateFieldNames.iterator(); fieldNameIter.hasNext();) {
//			String fieldName = fieldNameIter.next();
//			throw new SemanticException(
//					"Relational aspect "+at.fullName()+" is not allowed to declare field with name "+
//					fieldName+" (name clash with parameter "+fieldName+").");
//		}
		
		//check if super aspect is also a relational aspect with same signature
		Type superType = tc.typeSystem().superType(at);
		if(superType instanceof RelAspectType) {
			RelAspectType superAT = (RelAspectType) superType;
			if(superAT.relational()) {
				boolean equal = superAT.relationalAspectFormals().size() == at.relationalAspectFormals().size();				
				if(equal) {
					Iterator f2Iter = superAT.relationalAspectFormals().iterator();
					for (Iterator f1Iter = at.relationalAspectFormals().iterator(); f1Iter.hasNext();) {
						Formal f1 = (Formal) f1Iter.next();
						Formal f2 = (Formal) f2Iter.next();
						if(!f1.type().toString().equals(f2.type().toString())) {
							equal = false;
							break;
						}
					}
				}
				if(!equal) {
					throw new SemanticException(
							"Relational aspect "+at.fullName()+" with formals "+formals+" extends relational " +
							"aspect "+superAT.fullName()+" with formals "+superAT.relationalAspectFormals().toString().replaceAll("\\{amb\\}", "")+". " +
							"Formals have to coincide.");
				}
			} else {
				throw new SemanticException(
						"Relational aspect "+name()+" can only extend other relational aspects " +
						"with the same formal parameters.");
			}
		}
		
		return super.typeCheck(tc);
	}

	/**
	 * Copied from {@link AspectDecl_c}. Only difference is that a different
	 * type of aspect info is returned. 
	 */
	public void update(GlobalAspectInfo gai, Aspect current_aspect) {
		Per p = (per == null ? null : per.makeAIPer());
		AbcClass cl = AbcFactory.AbcClass(type());
    //changes here
		Aspect a = new RelationalAspect(cl, p, formals, tmBodyMethodNames, position());
	//end of changes
		gai.addAspect(a);

		List<abc.weaving.aspectinfo.Formal> fl = new ArrayList<abc.weaving.aspectinfo.Formal>();
		if (((RelAspectType) type()).perObject()) {
			fl.add(new abc.weaving.aspectinfo.Formal(AbcFactory.AbcType(soot.RefType
					.v("java.lang.Object")), "obj", position()));
		}

		List el = new ArrayList();
		// FIXME: Do these methods declare any exceptions?
		if (!flags().isAbstract()) {

			MethodSig aspectOf = new MethodSig(soot.Modifier.PUBLIC
					| soot.Modifier.STATIC, cl, AbcFactory.AbcType(type()),
					"aspectOf", fl, el, position());
			MethodSig hasAspect = new MethodSig(soot.Modifier.PUBLIC
					| soot.Modifier.STATIC, cl, AbcFactory
					.AbcType(soot.BooleanType.v()), "hasAspect", fl, el,
					position());

			MethodCategory.register(aspectOf, MethodCategory.ASPECT_INSTANCE);
			MethodCategory.register(hasAspect, MethodCategory.ASPECT_INSTANCE);
		}
	}
	
	protected RelAspectDecl_c reconstruct(List formals) {
	    if (CollectionUtil.equals(formals, this.formals)) {
			 RelAspectDecl_c n = (RelAspectDecl_c) copy();
			 n.formals = TypedList.copyAndCheck(formals, polyglot.ast.Formal.class, true);
			 return n;
		 }
		 return this;
	}

	public List<Formal> formals() {
		return formals;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public void addTmBodyMethodName(String tmBodyMethodName) {
		this.tmBodyMethodNames.add(tmBodyMethodName);
	}
}

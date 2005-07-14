/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import polyglot.util.CollectionUtil;
import polyglot.util.TypedList;
import polyglot.util.InternalCompilerError;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ClassDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.Expr;
import polyglot.ast.ClassBody;
import polyglot.ast.TypeNode;

import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.util.CodeWriter;
import polyglot.util.ErrorInfo;

import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.AmbiguityRemover;

import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.LocalInstance;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.visit.TypeChecker;
import polyglot.visit.AddMemberVisitor;

import abc.aspectj.extension.AJClassDecl_c;

import abc.aspectj.types.AJFlags;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AspectType;

import abc.aspectj.visit.AJTypeBuilder;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.ContainsAspectInfo;

import abc.weaving.aspectinfo.*;

/**
 *  A <code>AspectDecl</code> is the definition of an aspect, abstract aspect,
 *  or privileged. It may be a public or other top-level aspect, or an inner
 *  named aspect.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
 
public class AspectDecl_c extends AJClassDecl_c
                          implements AspectDecl,
                                     ContainsAspectInfo,
                                     MakesAspectMethods
{
    
    protected PerClause per;
    protected MethodInstance hasAspectInstance;
    protected MethodInstance aspectOfInstance;

   

    public AspectDecl_c(Position pos, boolean is_privileged, Flags flags, String name,
                        TypeNode superClass, List interfaces, PerClause per, AspectBody body) {
	     super(pos,
	           AJFlags.aspectclass(is_privileged ? AJFlags.privilegedaspect(flags): flags),
	           name,superClass,interfaces,body);
         this.per = per;
    }
    
    
    
    /**
     * construct a dummy aspectOf method that always returns null
	* it potentially throws org.aspectj.lang.NoAspectBoundException, so that is loaded,
	* if it is a per-object associated aspect, it takes one parameter of type Object, otherwise none
	*/
  
    private MethodDecl aspectOf(NodeFactory nf,AJTypeSystem ts) {
		TypeNode tn = nf.CanonicalTypeNode(position(),type).type(type);
		Expr nl = nf.NullLit(position()).type(type);
		Block bl = nf.Block(position()).append(nf.Return(position(),nl));
		TypeNode nab = nf.CanonicalTypeNode(position(),ts.NoAspectBound()).type(ts.NoAspectBound());
		List thrws = new LinkedList(); thrws.add(nab);
		List args = new LinkedList(); 
		if (((AspectType)type()).perObject()) {
			TypeNode obj = nf.CanonicalTypeNode(position(),ts.Object()).type(ts.Object());
			LocalInstance li = ts.localInstance(position(),Flags.NONE,ts.Object(),"thisparam");
			polyglot.ast.Formal f = nf.Formal(position(),Flags.NONE,obj,"thisparam").localInstance(li);
			args.add(f);
		}
		MethodDecl md = nf.MethodDecl(position(),Flags.PUBLIC.Static(),tn,"aspectOf",
		                       args,thrws,bl).methodInstance(aspectOfInstance); 
		return md; 
    }
    
    /**
     * construct a dummy hasAspect method that always returns true 
     */ 
    private MethodDecl hasAspect(NodeFactory nf, AJTypeSystem ts) {
    	TypeNode bool = nf.CanonicalTypeNode(position(),ts.Boolean()).type(ts.Boolean());
    	Expr b = nf.BooleanLit(position(),true).type(ts.Boolean());
    	Block bl = nf.Block(position()).append(nf.Return(position(),b));
    	
    	List args = new LinkedList();
		if (((AspectType)type()).perObject()) {
		    TypeNode obj = nf.CanonicalTypeNode(position(),ts.Object()).type(ts.Object());
		    LocalInstance li = ts.localInstance(position(),Flags.NONE,ts.Object(),"thisparam");
		    polyglot.ast.Formal f = nf.Formal(position(),Flags.NONE,obj,"thisparam").localInstance(li);
		    args.add(f);
		}
    	List thrws = new LinkedList();
    	MethodDecl md = nf.MethodDecl(position(),Flags.PUBLIC.Static(),bool,"hasAspect",
    	                 args,thrws,bl).methodInstance(hasAspectInstance);
    	return md;
    } 
    
    /** 
     * add the aspectOf and hasAspect methods to the aspect class, but only if it is concrete
    */
    
    public AspectDecl addAspectMembers(NodeFactory nf, AJTypeSystem ts) {
		if (!flags().isAbstract()) {
				MethodDecl aspectOf = aspectOf(nf,ts);
				MethodDecl hasAspect = hasAspect(nf,ts);
				return (AspectDecl) body(body().addMember(aspectOf).addMember(hasAspect)); 
		} else return this;
    }
    
	
	
	public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
		AJTypeBuilder ajtb = (AJTypeBuilder) tb;
	   	ajtb = ajtb.pushAspect(position(), flags, name, (per == null ? AspectType.PER_NONE : per.kind()));
        
		AspectType ct = (AspectType) ajtb.currentClass();

			
	   // Member classes of interfaces are implicitly public and static.
	   if (ct.isMember() && ct.outer().flags().isInterface()) {
		   ct.flags(ct.flags().Public().Static());
	   }

	   // Member interfaces are implicitly static. 
	   if (ct.isMember() && ct.flags().isInterface()) {
		   ct.flags(ct.flags().Static());
	   }

	   // Interfaces are implicitly abstract. 
	   if (ct.flags().isInterface()) {
		   ct.flags(ct.flags().Abstract());
	   }

	   return ajtb;
	}

	
	public NodeVisitor addMembersEnter(AddMemberVisitor am) {
	if (!flags().isAbstract()) {
		TypeSystem ts = am.typeSystem();
		List hasAspectparams = new ArrayList();
		List aspectOfparams = new ArrayList();
		if (((AspectType)type()).perObject()) {
			hasAspectparams.add(ts.Object());
			aspectOfparams.add(ts.Object());
		}
		List aspectOfthrows = new ArrayList();
		aspectOfthrows.add(((AJTypeSystem)ts).NoAspectBound());
	    hasAspectInstance = ts.methodInstance(position(),type,Flags.PUBLIC.Static(),ts.Boolean(),
                                            "hasAspect",hasAspectparams,new ArrayList());
        aspectOfInstance = ts.methodInstance(position(),type,Flags.PUBLIC.Static(),type,"aspectOf",
                                             aspectOfparams,aspectOfthrows);
		type.addMethod(hasAspectInstance);
		type.addMethod(aspectOfInstance);
	}
	return am;
	}
	
	public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
		   if (ar.kind() == AmbiguityRemover.SUPER) {
		   		List noSuper = new ArrayList();
		   		noSuper.add(body); noSuper.add(per);
			   	return ar.bypass(noSuper);
		   }
		   
		   if (ar.kind() == AmbiguityRemover.SIGNATURES) {
		   		return ar.bypass(per);
		   }

		   return ar;
	   }
		
	protected AspectDecl_c reconstruct(TypeNode superClass, List interfaces, PerClause per, ClassBody body) {
		   if (superClass != this.superClass || ! CollectionUtil.equals(interfaces, this.interfaces) || 
		        per != this.per || body != this.body) {
			   AspectDecl_c n = (AspectDecl_c) copy();
			   n.superClass = superClass;
			   n.interfaces = TypedList.copyAndCheck(interfaces, TypeNode.class, true);
			   n.per = per;
			   n.body = body;
			   return n;
		   }

		   return this;
	   }


	public Node visitChildren(NodeVisitor v) {
		TypeNode superClass = (TypeNode) visitChild(this.superClass, v);
		List interfaces = visitList(this.interfaces, v);
		PerClause per = (PerClause) visitChild(this.per,v);
		ClassBody body = (ClassBody) visitChild(this.body, v);
		return reconstruct(superClass, interfaces, per, body);
	}
	
	public Context enterScope(Node child, Context c) {
			if (child == this.per ) {
				TypeSystem ts = c.typeSystem();
				c = ((AJContext)c).pushAspect((AspectType) type);
				// c = c.pushClass(type, ts.staticTarget(type).toClass());
				return child.del().enterScope(c);
			}
			return super.enterScope(child, c);
		}
		
	public Context enterScope(Context c) {
		return ((AJContext)c).pushAspect((AspectType) type);
	}
	
	public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
		
		    // need to overwrite, because ClassDecl_c only knows of interfaces and classes
			w.write(AJFlags.clearAspectclass(flags).translate());
	        w.write("aspect ");

			w.write(name);

			if (superClass() != null) {
				w.write(" extends ");
				print(superClass(), w, tr);
			}

			if (! interfaces.isEmpty()) {
				if (flags.isInterface()) {
					w.write(" extends ");
				}
				else {
					w.write(" implements ");
				}

				for (Iterator i = interfaces().iterator(); i.hasNext(); ) {
					TypeNode tn = (TypeNode) i.next();
					print(tn, w, tr);

					if (i.hasNext()) {
						w.write (", ");
					}
				}
			}

			w.write(" {");
		}
		
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		if (type().isNested() && !flags().isStatic())
			throw new SemanticException("Nested aspects must be static",position());
		AspectDecl t = (AspectDecl) super.typeCheck(tc);
		TypeSystem ts = tc.typeSystem();
		Stack s = new Stack();
		s.push(t.type());
		Set visited = new HashSet();
		while (!s.isEmpty()) {
			ClassType ct = (ClassType) s.pop();
			if (visited.contains(ct))
				continue;
			visited.add(ct);
			if (ct.interfaces().contains(ts.Serializable()))
					throw new SemanticException("Aspects cannot implement Serializable",position());
			if (ct.interfaces().contains(ts.Cloneable()))
					throw new SemanticException("Aspects cannot implement Cloneable",position());
			if (ct.superType()!= null)
				s.push(ct.superType());
			s.addAll(ct.interfaces());
		}
		if  (superClass() != null &&
		     (superClass.type() instanceof AspectType) &&
		    ! superClass.type().toClass().flags().isAbstract())
			throw new SemanticException("Only abstract aspects can be extended",superClass.position());
		return t;
	}
	
    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	Per p = (per == null ? null : per.makeAIPer());
	AbcClass cl = AbcFactory.AbcClass(type());
	Aspect a = new Aspect(cl, p, position());
	gai.addAspect(a);
		    
	List fl = new ArrayList();
	if (((AspectType) type()).perObject()) {
	    fl.add(new Formal(AbcFactory.AbcType(soot.RefType.v("java.lang.Object")), "obj", position()));
	}

	List el = new ArrayList();
	// FIXME: Do these methods declare any exceptions?
	if(!flags().isAbstract()) {

	    MethodSig aspectOf = new MethodSig
		(soot.Modifier.PUBLIC | soot.Modifier.STATIC,
		 cl,
		 AbcFactory.AbcType(type()),
		 "aspectOf",
		 fl,
		 el,
		 position());
	    MethodSig hasAspect = new MethodSig
		(soot.Modifier.PUBLIC | soot.Modifier.STATIC,
		 cl,
		 AbcFactory.AbcType(soot.BooleanType.v()),
		 "hasAspect",
		 fl,
		 el,
		 position());
	    
	    MethodCategory.register(aspectOf, MethodCategory.ASPECT_INSTANCE);
	    MethodCategory.register(hasAspect, MethodCategory.ASPECT_INSTANCE);
	}
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushClass();
        visitor.pushContainer(type());
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        AspectDecl_c cd = this;
        List localMethods = visitor.methods();
        visitor.popClass();
        visitor.popContainer();

        for (Iterator i = localMethods.iterator(); i.hasNext(); ) {
            MethodDecl md = (MethodDecl) i.next();
            cd = (AspectDecl_c) this.body(cd.body().addMember(md));
        }
        
        cd.type().flags(cd.flags().Public());

        // add errors generaced by accessor methods to error queue
		for(Iterator it = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getNonWeavableClassErrors().iterator(); it.hasNext(); ) {
		    visitor.job().compiler().errorQueue().enqueue((ErrorInfo)it.next());
		}
		abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getNonWeavableClassErrors().clear();
        
        return cd.addAspectMembers(nf, ts).flags(cd.flags().Public());
    }
}

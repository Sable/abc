/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Julian Tibble
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

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.util.CodeWriter;

import polyglot.ast.TypeNode;
import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.Node;

import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.ClassType;
import polyglot.types.Context;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

import polyglot.ext.jl.ast.MethodDecl_c;

import abc.aspectj.ast.Pointcut;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AspectType;
import abc.aspectj.types.PointcutInstance;
import abc.aspectj.types.PointcutInstance_c;

import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.ContainsAspectInfo;
import abc.aspectj.visit.DependsCheck;
import abc.aspectj.visit.DependsChecker;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Aspect;


/** 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *  @author Julian Tibble
 */

public class PointcutDecl_c extends MethodDecl_c
                            implements PointcutDecl,
                                       ContainsAspectInfo,
                                       MakesAspectMethods,
                                       DependsCheck
{
    protected String name;
    protected Pointcut pc; // null if abstract

	  
    public PointcutDecl_c(Position pos,
                          Flags flags,
                          String name,
                          List formals,
                          Pointcut pc)
    {  super(pos,
			  flags, 
			  null, // no return type for pointcuts
			  name, 
			  formals,
			  new TypedList(new LinkedList(),TypeNode.class,true),
			  null);
        this.pc = pc;
        this.name = name;
    }
    
    //	new visitor code
	protected PointcutDecl_c reconstruct(List formals,
										Pointcut pc) {
	    if (pc != this.pc) {
			 PointcutDecl_c n = (PointcutDecl_c) copy();
			 n.pc = pc;
			 return (PointcutDecl_c) n.reconstruct(returnType(), formals, throwTypes(), body());
		 }
		 return (PointcutDecl_c) super.reconstruct(returnType(), formals, throwTypes(), body());
	 }
	 
	public Node visitChildren(NodeVisitor v) {	
			List formals = visitList(this.formals, v);
			Pointcut pc = (Pointcut) visitChild(this.pc,v);
			return reconstruct(formals, pc);
		}
		
		
/* ajc treats pointcuts as static
	public Context enterScope(Node child, Context c) {
		   Context nc = super.enterScope(child,c);
		   if (child==pc) // pointcuts should be treated as a static context
			   return nc.pushStatic();
		   else
			   return nc;
	  }
*/

	
	public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
		if (ar.kind() == AmbiguityRemover.SUPER) {
			return ar.bypassChildren(this);
		}
		else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
			return ar.bypass(pc);
		}

		return ar;
	}
		
	/** build the type */	
	public Node buildTypes(TypeBuilder tb) throws SemanticException {
				TypeSystem ts = tb.typeSystem();

				List l = new ArrayList(formals.size());
				for (int i = 0; i < formals.size(); i++) {
				  l.add(ts.unknownType(position()));
				}
				
		        List m = new ArrayList(throwTypes().size());
			    for (int i = 0; i < throwTypes().size(); i++) {
					  m.add(ts.unknownType(position()));
			    }

				MethodInstance mi = ((AJTypeSystem)ts).pointcutInstance(position(), ts.Object(),
													  Flags.NONE,
													  ts.unknownType(position()),
													  name, l, m);
				return methodInstance(mi);
			}

		protected MethodInstance makeMethodInstance(ClassType ct, TypeSystem ts)
			throws SemanticException {

			List argTypes = new LinkedList();
			List excTypes = new LinkedList();

			for (Iterator i = formals.iterator(); i.hasNext(); ) {
				Formal f = (Formal) i.next();
				argTypes.add(f.declType());
			}

			Flags flags = this.flags;

			if (ct.flags().isInterface()) {
				flags = flags.Public(); // but not abstract
			}
		    
			return ((AJTypeSystem)ts).pointcutInstance(position(),
										   ct, flags, ts.Void(), name,
										   argTypes,excTypes);
		}

	/** Type check the pointcut decl. */
   public Node typeCheck(TypeChecker tc) throws SemanticException {
	   TypeSystem ts = tc.typeSystem();
	   
      /* check the flags */
	  if (tc.context().currentClass().flags().isInterface()) {
			   if (flags().isProtected() || flags().isPrivate()) {
				   throw new SemanticException("Interface pointcuts must be public.",
											   position());
			   }
		   }
	   try {
		   ((AJTypeSystem)ts).checkPointcutFlags(flags());
	   }
	   catch (SemanticException e) {
		   throw new SemanticException(e.getMessage(), position());
	   }

	   if (!(pc instanceof PCEmpty) && flags().isAbstract()) {
		   throw new SemanticException(
		   "An abstract pointcut cannot have a body.", position());
	   }
		
	   if (!(methodInstance().container() instanceof AspectType)
	        && flags().isAbstract())
	   		throw new SemanticException("Abstract pointcut cannot be a member" +
	   		                            " of a class that is not an aspect.",position());
	  
	  	                            
	   overrideMethodCheck(tc);

	  if (!flags().isAbstract())
	  	pc.checkFormals(formals);
	  	
	  ((PointcutInstance)methodInstance()).setRefersTo(pc.pcRefs()) ;
	  ((PointcutInstance)methodInstance()).setDynamic(pc.isDynamic());
	  	
	   return this;
	}
	
	public Node checkDepends(DependsChecker dc ) throws SemanticException {
		PointcutInstance pci = (PointcutInstance) methodInstance();
		if (pci.cyclic())
			throw new SemanticException("Pointcuts cannot be recursive.",position());
		return this;
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.begin(0);
		w.write(flags.translate());
		w.write("pointcut " + name + "(");

		w.begin(0);

		for (Iterator i = formals.iterator(); i.hasNext(); ) {
	   		Formal f = (Formal) i.next();
	    	print(f, w, tr);

	    	if (i.hasNext()) {
			w.write(",");
			w.allowBreak(0, " ");
	    }
	}

	w.end();

	w.write(")");

	w.end();

	if (pc != null) 
	  {
            w.write(" :");
            w.allowBreak(0, " "); 
            print(pc, w, tr);
          }

	w.write(";");
    }
    
    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	abc.weaving.aspectinfo.PointcutDecl pcd =
	    new abc.weaving.aspectinfo.PointcutDecl
	    (name,
	     AspectInfoHarvester.convertFormals(formals()),
	     flags().isAbstract() ? null : pc.makeAIPointcut(),
	     current_aspect,
	     position());
	// Use the method instance as key
	AspectInfoHarvester.pointcutDeclarationMap().put(methodInstance(), pcd);
	gai.addPointcutDecl(pcd);
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushFormals(formals());
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        return this;
    }
}

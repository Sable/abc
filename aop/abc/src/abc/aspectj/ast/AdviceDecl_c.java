/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import polyglot.util.CodeWriter;
import polyglot.util.UniqueID;
import polyglot.util.Position;
import polyglot.util.TypedList;
import polyglot.util.InternalCompilerError;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;
import polyglot.ast.MethodDecl;
import polyglot.ast.Expr;
import polyglot.ast.Return;
import polyglot.ast.IntLit;
import polyglot.ast.CharLit;
import polyglot.ast.FloatLit;
import polyglot.ast.Local;


import polyglot.types.Flags;
import polyglot.types.Context;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.types.CodeInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.ClassType;
import polyglot.types.PrimitiveType;
import polyglot.types.ReferenceType;
import polyglot.types.ParsedClassType;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;
import polyglot.visit.TypeBuilder;
import polyglot.visit.CFGBuilder;

import polyglot.ext.jl.ast.MethodDecl_c;

import abc.aspectj.ast.AdviceFormal_c;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;

import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.ContainsAspectInfo;
import abc.aspectj.visit.AspectReflectionInspect;
import abc.aspectj.visit.AspectReflectionRewrite;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.AbcFactory;

/** 
 * 
 * Declarations of advice, for example
 * 
 *  <code> 
 * 	before(int x)      // the "advice spec"
 *    : 
 * 	call(* fac(*)) && args(x)  // pointcut
 *    { System.out.println(x);} // body
 *  </code>
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class AdviceDecl_c extends MethodDecl_c
    implements AdviceDecl, ContainsAspectInfo, MakesAspectMethods
{
	/** advice specification 
	 *  (e.g. <code> before(formals), around(..), after returning(..) </code>) */
    protected AdviceSpec spec;
    
    /** pointcut that specifies the joinpoints 
     *  where this advice applies */
    protected Pointcut pc;
    
    /** the return formal, for
     *  <code> afterreturning </code> and 
     *  <code> afterthrowing </code> advice */
    protected AdviceFormal retval;
    
    /** record whether <code> thisJoinPoint </code> occurs in the advice body.
     *   set by joinPointFormals(Local) */
    protected boolean hasJoinPoint=false;
	/** record whether <code> thisJoinPointStaticPart </code> occurs in the advice body.
	  *   set by joinPointFormals(Local) */
    protected boolean hasJoinPointStaticPart=false;
	/** record whether <code> thisEnclosingJoinPointStaticPart </code> occurs in the advice body.
	  *   set by joinPointFormals(Local) */
    protected boolean hasEnclosingJoinPointStaticPart=false;


    protected LocalInstance thisJoinPointInstance=null;
    protected LocalInstance thisJoinPointStaticPartInstance=null;
    protected LocalInstance thisEnclosingJoinPointStaticPartInstance=null;

    protected boolean canRewriteThisJoinPoint=false;
    

    protected Set/*<CodeInstance>*/ methodsInAdvice;
    
    protected int spec_retval_pos;

 
    public AdviceDecl_c(Position pos,
                        Flags flags,
                        AdviceSpec spec,
                        List throwTypes,
                        Pointcut pc,
	  	                Block body) {
		super(pos,
	    	  flags, 
	     	  spec.returnType(),
	     	  UniqueID.newID(spec.kind()),
	          spec.formals(),
	          throwTypes,
	          body);
	    this.methodsInAdvice = new HashSet();
		this.spec = spec;
    	this.pc = pc;
    	this.retval = spec.returnVal();
    }
    
    
    // new visitor code
	protected AdviceDecl_c reconstruct(TypeNode returnType, 
								       List formals, 
								       List throwTypes,
								       Block body,
								       AdviceSpec spec,
								       AdviceFormal retval,
								       Pointcut pc) {
		if (spec != this.spec || pc != this.pc || retval != this.retval) {
			AdviceDecl_c n = (AdviceDecl_c) copy();
			n.spec = spec;
			n.pc = pc;
			n.retval = retval;
			return (AdviceDecl_c) n.reconstruct(returnType, formals, throwTypes, body);
		}

		return (AdviceDecl_c) super.reconstruct(returnType, formals, throwTypes, body);
	}

	public Node visitChildren(NodeVisitor v) {	
		TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
		List formals = visitList(this.formals, v);
		List throwTypes = visitList(this.throwTypes, v);
		//AdviceSpec spec = (AdviceSpec) visitChild(this.spec, v);
		// FIXME: visiting spec gives duplicate errors!!
		AdviceFormal retval = (AdviceFormal) visitChild(this.retval,v);
		Pointcut pc = (Pointcut) visitChild(this.pc,v);
		Block body = (Block) visitChild(this.body, v);
		return reconstruct(returnType, formals, throwTypes, body, spec, retval, pc);
	}


	 public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
		 if (ar.kind() == AmbiguityRemover.SUPER) {
			 return ar.bypassChildren(this);
		 }
		 else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
			 if (body != null) {
			 	Collection bp = new LinkedList();
			 	bp.add(body);
			 	bp.add(pc);
			 	bp.add(retval);
				return ar.bypass(bp);
			 }
		 }

		 return ar;
	 }
	 
    
    private Expr dummyVal(AJNodeFactory nf, Type t) {
    	if (t instanceof ReferenceType) 
    		return nf.NullLit(position());
    	if (t instanceof PrimitiveType) {
    		PrimitiveType pt = (PrimitiveType) t;
    		if (pt.isChar())
    			return nf.CharLit(position(),'x');
    		if (pt.isBoolean())
    			return nf.BooleanLit(position(),true);
    		if (pt.isByte())
    			return nf.IntLit(position(),IntLit.INT,0);
    		if (pt.isShort())
    			return nf.IntLit(position(),IntLit.INT,0);
    		if (pt.isInt())
    			return nf.IntLit(position(),IntLit.INT,0);
    		if (pt.isLong())
    			return nf.IntLit(position(),IntLit.LONG,0);
    		if (pt.isFloat())
    			return nf.FloatLit(position(),FloatLit.FLOAT,0.0);
    		if (pt.isDouble())
    			return nf.FloatLit(position(),FloatLit.DOUBLE,0.0);	
    		if (pt.isVoid())
    			throw new InternalCompilerError("cannot create expression of void type");
    		else return null;
    	} else return null;
    }
    
    public MethodDecl proceedDecl(AJNodeFactory nf,
                                                                AJTypeSystem ts) {
    if (spec instanceof Around) {
    	TypeNode tn = (TypeNode) returnType().copy();
		List formals = new LinkedList(formals());
		Return ret;
		if (tn.type() == ts.Void())
			ret = nf.Return(position());
		else {
			Expr dummy = dummyVal(nf, tn.type());
			ret = nf.Return(position(),dummy);
		}
		Block bl = nf.Block(position()).append(ret);
		List thrws = new LinkedList(/*throwTypes()*/); 
		String name = UniqueID.newID("proceed");
		MethodDecl md = nf.MethodDecl(position(),
						Flags.PUBLIC.set(Flags.FINAL).Static()
						,tn,name,formals,thrws,bl);      
		MethodInstance mi = ts.methodInstance(position(), methodInstance().container(),
						      Flags.PUBLIC.set(Flags.FINAL).Static()
						      , tn.type(), name,
						      new ArrayList(methodInstance().formalTypes()),
						      new ArrayList(/*methodInstance().throwTypes()*/));
		((ParsedClassType)methodInstance().container()).addMethod(mi);
		md = md.methodInstance(mi);
		((Around)spec).setProceed(md);
		return md;
    } else return null;
    }
    
    
    public boolean hasJoinPointStaticPart() {
    	return hasJoinPointStaticPart;
    }
    
    public boolean hasJoinPoint() {
    	return hasJoinPoint;
    }
    
    public boolean hasEnclosingJoinPointStaticPart() {
    	return hasEnclosingJoinPointStaticPart;
    }
    
    public void joinpointFormals(Local n) {
    	hasJoinPoint = hasJoinPoint || (n.name().equals("thisJoinPoint"));
    	hasJoinPointStaticPart = hasJoinPointStaticPart || (n.name().equals("thisJoinPointStaticPart"));
    	hasEnclosingJoinPointStaticPart = hasEnclosingJoinPointStaticPart || (n.name().equals("thisEnclosingJoinPointStaticPart"));
    }
    
    public MethodDecl methodDecl(AJNodeFactory nf,
    															AJTypeSystem ts) {
    	List newformals = new LinkedList(formals());
    	List newformalTypes = new LinkedList(formals());
    	if (retval != null) {
    		newformals.add(retval);
    		newformalTypes.add(retval.type());
    	}
	// Add joinpoint parameters
    	if (hasJoinPointStaticPart()) {
    		TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart())
    		              .type(ts.JoinPointStaticPart());
    		Formal jpsp = nf.Formal(position(),Flags.FINAL,tn,"thisJoinPointStaticPart");
		    LocalInstance li = thisJoinPointStaticPartInstance(ts);
		    jpsp = jpsp.localInstance(li);
    		newformals.add(jpsp);
    		newformalTypes.add(ts.JoinPointStaticPart());
    	}
    	if (hasJoinPoint()) {
    		TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPoint())
    		              .type(ts.JoinPoint());
    		Formal jp = nf.Formal(position(),Flags.FINAL,tn,"thisJoinPoint");
		    LocalInstance li = thisJoinPointInstance(ts);
		    jp = jp.localInstance(li);
    		newformals.add(jp);
    		newformalTypes.add(ts.JoinPoint());
    	}
		if (hasEnclosingJoinPointStaticPart()) {
			TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart())
			              .type(ts.JoinPointStaticPart());
			Formal jp = nf.Formal(position(),Flags.FINAL,tn,"thisEnclosingJoinPointStaticPart");
			LocalInstance li = thisEnclosingJoinPointStaticPartInstance(ts);
			jp = jp.localInstance(li);
			newformals.add(jp);
			newformalTypes.add(ts.JoinPoint());
		}
		Flags f = this.flags().set(Flags.FINAL).set(Flags.PUBLIC);
    	MethodDecl md = reconstruct(returnType(),newformals,throwTypes(),body(),spec,retval,pc).flags(f);
    	MethodInstance mi = md.methodInstance().formalTypes(newformalTypes).flags(f);
	//nf.MethodDecl(position(),Flags.PUBLIC,returnType(),name,newformals,throwTypes(),body());
    	return md.methodInstance(mi);
    }

   
    private LocalInstance thisJoinPointInstance(AJTypeSystem ts) {
    	if (thisJoinPointInstance==null)
    		thisJoinPointInstance = ts.localInstance(position(),Flags.FINAL,ts.JoinPoint(),"thisJoinPoint");
    	return thisJoinPointInstance;
    }
    
	private LocalInstance thisJoinPointStaticPartInstance(AJTypeSystem ts) {
		 if (thisJoinPointStaticPartInstance==null)
			 thisJoinPointStaticPartInstance = ts.localInstance(position(),Flags.FINAL,ts.JoinPointStaticPart(),"thisJoinPointStaticPart");
		 return thisJoinPointStaticPartInstance;
	 }
	 
 	private LocalInstance thisEnclosingJoinPointStaticPartInstance(AJTypeSystem ts) {
		if (thisEnclosingJoinPointStaticPartInstance==null)
			thisEnclosingJoinPointStaticPartInstance = ts.localInstance(position(),Flags.FINAL,
			                                                            ts.JoinPointStaticPart(),"thisEnclosingJoinPointStaticPart");
		return thisEnclosingJoinPointStaticPartInstance;
	}
	
	 
	public Context enterScope(Context c) {
			Context nc = super.enterScope(c);
			AJContext nnc = ((AJContext) nc).pushAdvice(spec instanceof Around);
		return nnc;
	}
		
	public Context enterScope(Node child, Context c) {
		if (child==body) {
			AJContext ajc = (AJContext) child.enterScope(c);
			 AJTypeSystem ts = (AJTypeSystem)ajc.typeSystem();
			 LocalInstance jp = thisJoinPointInstance(ts);
			 ajc.addVariable(jp);
			 LocalInstance sjp = thisJoinPointStaticPartInstance(ts);
			 ajc.addVariable(sjp);
			 LocalInstance ejpsp = thisEnclosingJoinPointStaticPartInstance(ts);
			 ajc.addVariable(ejpsp);
			if (spec instanceof Around){
		   	 LinkedList l = new LinkedList();
		   	 l.add(ts.Throwable());
		   	 MethodInstance proceedInstance = methodInstance().name("proceed").flags(flags().Public().Static()).throwTypes(l);
		    	ajc.addProceed(proceedInstance);
			}	
			if (retval != null) {
				ajc.addVariable(retval.localInstance());
			}
			return ajc;
		}
		return super.enterScope(child,c);
	}
	
	/** Type check the advice: first the usual method checks, then whether the "throwing" result is
	 *  actually throwable
	 *  */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		super.typeCheck(tc);
		if (spec instanceof AfterThrowing && retval != null) {
			
			Type t = retval.type().type();
			if (! t.isThrowable()) {
				TypeSystem ts = tc.typeSystem();
				throw new SemanticException("type \"" + t + "\" is not a subclass of \" +" +					                        ts.Throwable() + "\".", spec.returnVal().type().position());
			}
		}
		
		pc.checkFormals(formals);
		
		Flags f = flags().clear(Flags.STRICTFP);
		if (!f.equals(Flags.NONE))
			throw new SemanticException("advice cannot have flags "+f,position());
	  
		return this;
	}
	
	
		
	/** build the type; the spec is included in the advice instance to give
	 *  intelligible error messages - see adviceInstance overrides
	 */	
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

			MethodInstance mi = ((AJTypeSystem)ts).adviceInstance(position(), ts.Object(),
												  Flags.NONE,
												  ts.unknownType(position()),
												  name, l, m, spec);
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

		for (Iterator i = throwTypes().iterator(); i.hasNext(); ) {
			TypeNode tn = (TypeNode) i.next();
			excTypes.add(tn.type());
		}

		Flags flags = this.flags;

		if (ct.flags().isInterface()) {
			flags = flags.Public().Abstract();
		}
	
	
		return ((AJTypeSystem)ts).adviceInstance(position(),
								       ct, flags, returnType.type(), name,
								       argTypes, excTypes,spec);
	}
		
	public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
			w.begin(0);
			w.write(flags.translate());

			print(spec,w,tr);

			w.begin(0);

			if (! throwTypes.isEmpty()) {
				w.allowBreak(6);
				w.write("throws ");

				for (Iterator i = throwTypes.iterator(); i.hasNext(); ) {
					TypeNode tn = (TypeNode) i.next();
					print(tn, w, tr);
	
					if (i.hasNext()) {
						w.write(",");
						w.allowBreak(4, " ");
					}
				}
			}

			w.end();

			w.write(":");

			w.allowBreak(0);

			print(pc, w, tr);
	
			if (body != null) 
			   printSubStmt(body, w, tr);
			else w.write(";");

			w.end();
		}
		
	public void localMethod(CodeInstance ci) {
		methodsInAdvice.add(ci);
	}

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	int lastpos = formals().size();
	int jp = -1, jpsp = -1, ejp = -1;
	if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
	if (hasJoinPoint) jp = --lastpos;
	if (hasJoinPointStaticPart) jpsp = --lastpos;

	// Since the spec is not visited, we copy the (checked)
	// return type node from the advice declaration
	spec.setReturnType(returnType());
	// And the return formal as well
	if (retval != null) {
	    spec.setReturnVal(retval);
	}
	
	List methods = new ArrayList();
	for (Iterator procs = methodsInAdvice.iterator(); procs.hasNext(); ) {
		CodeInstance ci = (CodeInstance) procs.next();
		if (ci instanceof MethodInstance)
			methods.add(AbcFactory.MethodSig((MethodInstance)ci));
		if (ci instanceof ConstructorInstance)
			methods.add(AbcFactory.MethodSig((ConstructorInstance)ci));
	}

	abc.weaving.aspectinfo.AdviceDecl ad =
	    new abc.weaving.aspectinfo.AdviceDecl
	    (spec.makeAIAdviceSpec(),
	     pc.makeAIPointcut(),
	     AbcFactory.MethodSig(this),
	     current_aspect,
	     jp, jpsp, ejp, methods,
	     position());
	gai.addAdviceDecl(ad);
	


	MethodCategory.register(this, MethodCategory.ADVICE_BODY);
	if (spec instanceof Around) {
	    MethodCategory.register(((Around)spec).proceed(), MethodCategory.PROCEED);
	}
    }

    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushProceedFor(this);
        visitor.pushFormals(formals());
        visitor.pushAdvice(this);
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        MethodDecl md = visitor.proceed();

        visitor.popAdvice();
        visitor.popFormals();
        visitor.popProceed();

        if (md != null)
            visitor.addMethod(md);

        return this.methodDecl(nf,ts);
    }

    public void enterAspectReflectionInspect(AspectReflectionInspect v,Node parent) {
	v.enterAdvice();
    }

    public void leaveAspectReflectionInspect(AspectReflectionInspect v) {
	canRewriteThisJoinPoint=v.leaveAdvice();
    }

    public void enterAspectReflectionRewrite(AspectReflectionRewrite v,AJTypeSystem ts) {
	v.enterAdvice(canRewriteThisJoinPoint ? thisJoinPointStaticPartInstance(ts) : null);
    }

    public Node leaveAspectReflectionRewrite(AspectReflectionRewrite v,AJNodeFactory nf) {
	v.leaveAdvice();
	return this;
    }
    
	/**
	  * Visit this term in evaluation order.
	  */
	public List acceptCFG(CFGBuilder v, List succs) {
		if (retval==null)
			return super.acceptCFG(v,succs);
		 if (body() == null) {
			 v.visitCFGList(formals(), retval.entry());
			 v.visitCFG(retval, this);
		 }
		 else {
			 v.visitCFGList(formals(), retval.entry());
			 v.visitCFG(retval, body().entry());
			 v.visitCFG(body(), this);
		 }
		 return succs;
	 }

}

package abc.aspectj.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;

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

import polyglot.ext.jl.ast.MethodDecl_c;

import abc.aspectj.ast.AdviceFormal_c;

import abc.aspectj.types.AspectJTypeSystem;

import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.ContainsAspectInfo;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.MethodCategory;

public class AdviceDecl_c extends MethodDecl_c
    implements AdviceDecl, ContainsAspectInfo
{
    protected AdviceSpec spec;
    protected Pointcut pc;
    protected boolean hasJoinPoint;
    protected boolean hasJoinPointStaticPart;
    protected boolean hasEnclosingJoinPointStaticPart;

    protected LocalInstance thisJoinPointInstance;
    protected LocalInstance thisJoinPointStaticPartInstance;
    protected LocalInstance thisEnclosingJoinPointStaticPartInstance;
    
    protected int spec_retval_pos;

    // if the returnVal of "after returning" or "after throwing" is
    // specified, make it an additional parameter to the advice body
    private static List locs(Formal rt, List formals) {
    	if (rt==null)
    	  return formals;
    	else {
    		List locs = ((TypedList)formals).copy();
    		locs.add(rt);
    		return locs;
    	}
    }
    
    private static List adviceFormals(List formals) {
    	List result = new TypedList(new LinkedList(), AdviceFormal.class, false);
    	for (Iterator i = formals.iterator(); i.hasNext(); ) {
    		Formal f = (Formal) i.next();
    		result.add(new AdviceFormal_c(f));
    	}
    	return result;
    }

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
	          adviceFormals(locs(spec.returnVal(),spec.formals())),
	          throwTypes,
	          body);
		this.spec = spec;
    	this.pc = pc;

	if (spec.returnVal() != null) {
	    spec_retval_pos = formals().size()-1;
	} else {
	    spec_retval_pos = -1;
	}
    }
    
    
    // new visitor code
	protected AdviceDecl_c reconstruct(TypeNode returnType, 
								       List formals, 
								       List throwTypes,
								       Block body,
								       AdviceSpec spec,
								       Pointcut pc) {
		if (spec != this.spec || pc != this.pc) {
			AdviceDecl_c n = (AdviceDecl_c) copy();
			n.spec = spec;
			n.pc = pc;
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
		Pointcut pc = (Pointcut) visitChild(this.pc,v);
		Block body = (Block) visitChild(this.body, v);
		return reconstruct(returnType, formals, throwTypes, body, spec, pc);
	}

/* ajc treats pointcuts as static contexts 
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
			 if (body != null) {
			 	Collection bp = new LinkedList();
			 	bp.add(body);
			 	bp.add(pc);
				return ar.bypass(bp);
			 }
		 }

		 return ar;
	 }
	 
    
    private Expr dummyVal(AspectJNodeFactory nf, Type t) {
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
    
    public MethodDecl proceedDecl(AspectJNodeFactory nf,
                                                                AspectJTypeSystem ts) {
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
		List thrws = new LinkedList(throwTypes()); 
		String name = UniqueID.newID("proceed");
		MethodDecl md = nf.MethodDecl(position(),Flags.PRIVATE,tn,name,formals,thrws,bl);      
		MethodInstance mi = ts.methodInstance(position(), methodInstance().container(),
						      Flags.PRIVATE, tn.type(), name,
						      new ArrayList(methodInstance().formalTypes()),
						      new ArrayList(throwTypes()));
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
    
    public MethodDecl methodDecl(AspectJNodeFactory nf,
    															AspectJTypeSystem ts) {
    	List newformals = new LinkedList(formals());
    	List newformalTypes = new LinkedList(formals());
	// Add enclosing joinpoint here
    	if (hasJoinPointStaticPart()) {
    		TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart());
    		Formal jpsp = nf.Formal(position(),Flags.FINAL,tn,"thisJoinPointStaticPart");
		    LocalInstance li = thisJoinPointStaticPartInstance(ts);
		    jpsp = jpsp.localInstance(li);
    		newformals.add(jpsp);
    		newformalTypes.add(ts.JoinPointStaticPart());
    	}
    	if (hasJoinPoint()) {
    		TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPoint());
    		Formal jp = nf.Formal(position(),Flags.FINAL,tn,"thisJoinPoint");
		    LocalInstance li = thisJoinPointInstance(ts);
		    jp = jp.localInstance(li);
    		newformals.add(jp);
    		newformalTypes.add(ts.JoinPoint());
    	}
		if (hasEnclosingJoinPointStaticPart()) {
			TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart());
			Formal jp = nf.Formal(position(),Flags.FINAL,tn,"thisEnclosingJoinPointStaticPart");
			LocalInstance li = thisEnclosingJoinPointStaticPartInstance(ts);
			jp = jp.localInstance(li);
			newformals.add(jp);
			newformalTypes.add(ts.JoinPoint());
		}
		Flags f = this.flags().set(Flags.FINAL);
    	MethodDecl md = reconstruct(returnType(),newformals,throwTypes(),body(),spec,pc).flags(f);
    	MethodInstance mi = md.methodInstance().formalTypes(newformalTypes).flags(f);
	//nf.MethodDecl(position(),Flags.PUBLIC,returnType(),name,newformals,throwTypes(),body());
    	return md.methodInstance(mi);
    }

    /** Type checking of proceed: keep track of the methodInstance for the current proceed
     *  the ProceedCall will query this information via the proceedInstance() 
     *  method
     * */
	static private MethodInstance proceedInstance = null;
	static private Context scope = null;
	static public MethodInstance  proceedInstance(Context c) {
		if (c==null)
		   return null;
		if (c==scope)
		   return proceedInstance;
		else return proceedInstance(c.pop());
	}

    private LocalInstance thisJoinPointInstance(AspectJTypeSystem ts) {
    	if (thisJoinPointInstance==null)
    		thisJoinPointInstance = ts.localInstance(position(),Flags.FINAL,ts.JoinPoint(),"thisJoinPoint");
    	return thisJoinPointInstance;
    }
    
	private LocalInstance thisJoinPointStaticPartInstance(AspectJTypeSystem ts) {
		 if (thisJoinPointStaticPartInstance==null)
			 thisJoinPointStaticPartInstance = ts.localInstance(position(),Flags.FINAL,ts.JoinPointStaticPart(),"thisJoinPointStaticPart");
		 return thisJoinPointStaticPartInstance;
	 }
	 
 	private LocalInstance thisEnclosingJoinPointStaticPartInstance(AspectJTypeSystem ts) {
		if (thisEnclosingJoinPointStaticPartInstance==null)
			thisEnclosingJoinPointStaticPartInstance = ts.localInstance(position(),Flags.FINAL,
			                                                            ts.JoinPointStaticPart(),"thisEnclosingJoinPointStaticPart");
		return thisEnclosingJoinPointStaticPartInstance;
	}
	
	 
	public Context enterScope(Context c) {
		Context nc = super.enterScope(c);
		
		// inside an advice body, thisJoinPoint is in scope, but nowhere else in an aspect
		AspectJTypeSystem ts = (AspectJTypeSystem)nc.typeSystem();
		LocalInstance jp = thisJoinPointInstance(ts);
		nc.addVariable(jp);
		LocalInstance sjp = thisJoinPointStaticPartInstance(ts);
		nc.addVariable(sjp);
		LocalInstance ejpsp = thisEnclosingJoinPointStaticPartInstance(ts);
		nc.addVariable(ejpsp);
		
		if (spec instanceof Around)
			proceedInstance = methodInstance().name("proceed");
		else
		    proceedInstance = null;
		scope = nc;
               
		return nc;
	}
	
	/** Type check the advice: first the usual method checks, then whether the "throwing" result is
	 *  actually throwable
	 *  */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		super.typeCheck(tc);
		if (spec instanceof AfterThrowing && spec.returnVal() != null) {
			
			// get the resolved type of the returnVal, which is the last parameter
			// of the advice method:
			List formalTypes = methodInstance().formalTypes();
			Type t = (Type)(formalTypes.get(formalTypes.size()-1));
			
			if (! t.isThrowable()) {
				TypeSystem ts = tc.typeSystem();
				throw new SemanticException("type \"" + t + "\" is not a subclass of \" +" +					                        ts.Throwable() + "\".", spec.returnVal().type().position());
			}
		}
		
		Formal initialised = null;
		if (spec.returnVal() != null) 
			initialised = (Formal)  formals.get(formals.size()-1); // last parameter is always initialised
		pc.checkFormals(formals,initialised);
	  
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

			MethodInstance mi = ((AspectJTypeSystem)ts).adviceInstance(position(), ts.Object(),
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
	
	
		return ((AspectJTypeSystem)ts).adviceInstance(position(),
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
	if (spec_retval_pos != -1) {
	    spec.setReturnVal((Formal)formals().get(spec_retval_pos));
	}

	abc.weaving.aspectinfo.AdviceDecl ad =
	    new abc.weaving.aspectinfo.AdviceDecl
	    (spec.makeAIAdviceSpec(),
	     pc.makeAIPointcut(),
	     AspectInfoHarvester.makeMethodSig(this),
	     current_aspect,
	     jp, jpsp, ejp,
	     position());
	gai.addAdviceDecl(ad);

	MethodCategory.register(this, MethodCategory.ADVICE_BODY);
	if (spec instanceof Around) {
	    MethodCategory.register(((Around)spec).proceed(), MethodCategory.PROCEED);
	}
    }

}
	

	

     



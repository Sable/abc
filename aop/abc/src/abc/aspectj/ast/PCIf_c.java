package abc.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.TransformsAspectReflection;
import abc.aspectj.visit.AspectReflectionInspect;
import abc.aspectj.visit.AspectReflectionRewrite;

import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.AbcFactory;

public class PCIf_c extends Pointcut_c implements PCIf, MakesAspectMethods
{
    protected Expr expr;
    private String methodName;
    private MethodDecl methodDecl;

    public PCIf_c(Position pos, Expr expr)  {
	super(pos);
        this.expr = expr;
    }

    public Precedence precedence() {
		return Precedence.LITERAL;
    }
    
	public Set pcRefs() {
		return new HashSet();
	}
	

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write("if(");
        print(expr, w, tr);
        w.write(")");
    }
    
	/** Reconstruct the pointcut. */
	protected PCIf_c reconstruct(Expr expr) {
	   if (expr != this.expr) {
		   PCIf_c n = (PCIf_c) copy();
		   n.expr = expr;
		   return n;
	   }

	   return this;
	}

	/** Visit the children of the pointcut. */
	public Node visitChildren(NodeVisitor v) {
	   Expr expr = (Expr) visitChild(this.expr, v);
	   return reconstruct(expr);
	}

    
	/** Type check the pointcut. */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		TypeSystem ts = tc.typeSystem();

		AJContext c = (AJContext) tc.context();
		if (c.inDeclare())
			throw new SemanticException("if(..) requires a dynamic test and cannot be used inside a \"declare\" statement", position());
		
		if (! ts.equals(expr.type(), ts.Boolean())) {
			throw new SemanticException(
			"Condition of if pointcut must have boolean type.",
			expr.position());
		}
		
		return this;
	}

	public Type childExpectedType(Expr child, AscriptionVisitor av) {
		TypeSystem ts = av.typeSystem();

		if (child == expr) {
			return ts.Boolean();
		}

		return child.type();
	}

    protected boolean hasJoinPoint=false;
    protected boolean hasJoinPointStaticPart=false;
    protected boolean hasEnclosingJoinPointStaticPart=false;

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
    	hasEnclosingJoinPointStaticPart = hasEnclosingJoinPointStaticPart 
	    || (n.name().equals("thisEnclosingJoinPointStaticPart"));
    }

    protected LocalInstance thisJoinPointInstance=null;
    protected LocalInstance thisJoinPointStaticPartInstance=null;
    protected LocalInstance thisEnclosingJoinPointStaticPartInstance=null;
    
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
	

    protected boolean canRewriteThisJoinPoint=false;

    public MethodDecl exprMethod(AJNodeFactory nf, AJTypeSystem ts, List formals, ParsedClassType container){
		Return ret = nf.Return(position(),expr);
		Block bl = nf.Block(position()).append(ret);
		TypeNode retType = nf.CanonicalTypeNode(position(),ts.Boolean());
		List args = new LinkedList(formals);
		List throwTypes = new LinkedList();
		for (Iterator i = expr.throwTypes(ts).iterator(); i.hasNext(); ) {
			Type t = (Type) i.next();
			TypeNode tn = nf.CanonicalTypeNode(position(),t);
			throwTypes.add(tn);
		}
		List formaltypes = new ArrayList();
		Iterator fi = formals.iterator();
		while (fi.hasNext()) {
		    Formal f = (Formal)fi.next();
		    formaltypes.add(f.type().type());
		}
		if (hasJoinPointStaticPart()) {
		    TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart());
		    Formal jpsp = nf.Formal(position(),Flags.FINAL,tn,"thisJoinPointStaticPart");
		    LocalInstance li = thisJoinPointStaticPartInstance(ts);
		    jpsp = jpsp.localInstance(li);
		    args.add(jpsp);
		    formaltypes.add(ts.JoinPointStaticPart());
		}
		if (hasJoinPoint()) {
		    TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPoint());
		    Formal jp = nf.Formal(position(),Flags.FINAL,tn,"thisJoinPoint");
		    LocalInstance li = thisJoinPointInstance(ts);
		    jp = jp.localInstance(li);
		    args.add(jp);
		    formaltypes.add(ts.JoinPoint());
		}
		if (hasEnclosingJoinPointStaticPart()) {
		    TypeNode tn = nf.CanonicalTypeNode(position(),ts.JoinPointStaticPart());
		    Formal jp = nf.Formal(position(),Flags.FINAL,tn,"thisEnclosingJoinPointStaticPart");
		    LocalInstance li = thisEnclosingJoinPointStaticPartInstance(ts);
		    jp = jp.localInstance(li);
		    args.add(jp);
		    formaltypes.add(ts.JoinPoint());
		}
		methodName = UniqueID.newID("if");
		MethodDecl md = nf.MethodDecl(position(),Flags.STATIC.Public(),retType,methodName,args,throwTypes,bl);
		MethodInstance mi = ts.methodInstance(position, container,
						      Flags.STATIC.Public(), retType.type(), methodName,
						      new ArrayList(formaltypes),
						      new ArrayList(expr.throwTypes(ts)));
		container.addMethod(mi);
		md = md.methodInstance(mi);
		methodDecl = md;
		return md;
	}
	
	public PCIf liftMethod(AJNodeFactory nf){
		Expr exp = nf.Call(position(),methodName);
		return reconstruct(exp);
	}

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	int lastpos = methodDecl.formals().size();
	int jp = -1, jpsp = -1, ejp = -1;
	if (hasEnclosingJoinPointStaticPart) ejp = --lastpos;
	if (hasJoinPoint) jp = --lastpos;
	if (hasJoinPointStaticPart) jpsp = --lastpos;

	MethodCategory.register(methodDecl, MethodCategory.IF_EXPR);

	List vars = new ArrayList();
	Iterator fi = methodDecl.formals().iterator();
	while (fi.hasNext()) {
	    Formal f = (Formal)fi.next();
	    vars.add(new abc.weaving.aspectinfo.Var(f.name(), f.position()));
	}
	return new abc.weaving.aspectinfo.If
	    (vars, AbcFactory.MethodSig(methodDecl),jp,jpsp,ejp,position);
    }


    public Context enterScope(Context c) {
    	AJContext ajc = (AJContext) c.pushStatic();
		AJTypeSystem ts = (AJTypeSystem)ajc.typeSystem();
		LocalInstance jp = thisJoinPointInstance(ts);
		ajc.addVariable(jp);
		LocalInstance sjp = thisJoinPointStaticPartInstance(ts);
		ajc.addVariable(sjp);
		LocalInstance ejpsp = thisEnclosingJoinPointStaticPartInstance(ts);
		ajc.addVariable(ejpsp);
		return ajc;
    }  

    public void aspectMethodsEnter(AspectMethods visitor)
    {
	visitor.pushPCIf(this);
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        // construct method for expression in if(..)
        MethodDecl md = exprMethod(nf, ts, visitor.formals(), visitor.container());
        visitor.addMethod(md);
	visitor.popPCIf();
        return liftMethod(nf); // replace expression by method call
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
}

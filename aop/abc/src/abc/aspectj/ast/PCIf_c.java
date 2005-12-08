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
import abc.main.Debug;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class PCIf_c extends Pointcut_c implements PCIf, MakesAspectMethods
{
    protected Expr expr;
    protected String methodName;
    protected MethodDecl methodDecl;

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
	
	public boolean isDynamic() {
		return true;
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
		if (c.inDeclare() && !Debug.v().allowDynamicTests)
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

                addJoinPointFormals(nf, ts, args, formaltypes);

		methodName = UniqueID.newID("if");
		MethodDecl md = nf.MethodDecl(position(),Flags.STATIC.Public(),retType,methodName,args,throwTypes,bl);
		MethodInstance mi = ts.methodInstance(position, container,
						      Flags.STATIC.Public(), retType.type(), methodName,
						      new ArrayList(formaltypes),
						      new ArrayList(expr.del().throwTypes(ts)));
		container.addMethod(mi);
		md = md.methodInstance(mi);
		methodDecl = md;
		return md;
	}

        protected void addJoinPointFormals(AJNodeFactory nf, AJTypeSystem ts,
                                           List args, List formaltypes)
        {
		if (hasJoinPointStaticPart()) {
		    addJoinPointFormal(nf, ts, args, formaltypes,
		                       ts.JoinPointStaticPart(),
                                       "thisJoinPointStaticPart",
                                       thisJoinPointStaticPartInstance(ts));
                }
		if (hasJoinPoint()) {
		    addJoinPointFormal(nf, ts, args, formaltypes,
		                       ts.JoinPoint(),
                                       "thisJoinPoint",
                                       thisJoinPointInstance(ts));
                }
		if (hasEnclosingJoinPointStaticPart()) {
		    addJoinPointFormal(nf, ts, args, formaltypes,
		                       ts.JoinPointStaticPart(),
                                       "thisEnclosingJoinPointStaticPart",
                                       thisEnclosingJoinPointStaticPartInstance(ts));
                }
        }

        protected void addJoinPointFormal(AJNodeFactory nf, AJTypeSystem ts,
                                          List args, List formaltypes,
                                          ClassType jpfType, String name,
                                          LocalInstance li)
        {
            TypeNode tn = nf.CanonicalTypeNode(position(), jpfType);
            Formal jpf = nf.Formal(position(), Flags.FINAL, tn, name);
            jpf = jpf.localInstance(li);
            args.add(jpf);
            formaltypes.add(jpfType);
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
    	AJContext ajc = ((AJContext) c.pushStatic()).pushIf();
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
        List formals = calculateMethodParameters(visitor, nf, ts);

        MethodDecl md = exprMethod(nf, ts, formals, visitor.container());
        visitor.addMethod(md);
	visitor.popPCIf();
        return liftMethod(nf); // replace expression by method call
    }

    protected List calculateMethodParameters(AspectMethods visitor,
                                            AJNodeFactory nf, AJTypeSystem ts)
    {
        // construct method for expression in if(..).
        // When the if(..) occurs inside a cflow, the parameters are only the
        // variables bound inside that cflow. Otherwise, the parameters are exactly
        // the formals of the enclosing named pointcut or advice.
        AJContext ajc = (AJContext) visitor.context();
        List formals = new ArrayList();
        if (ajc.inCflow()) {
            Collection cflowVars = ajc.getCflowMustBind();
            for (Iterator varit = cflowVars.iterator(); varit.hasNext(); ) {
                String varName = (String) varit.next();
                LocalInstance li = (LocalInstance) ajc.findVariableSilent(varName);
                TypeNode tn = nf.CanonicalTypeNode(li.position(),li.type());
                Formal vf = nf.Formal(li.position(),Flags.FINAL,tn,varName).localInstance(li);
                formals.add(vf);
            }
        } else formals = visitor.formals();

        return formals;
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

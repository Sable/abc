package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.types.SemanticException;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.types.Context_c;

import arc.aspectj.types.AspectJTypeSystem;
import arc.aspectj.types.PointcutInstance_c;

public class PCName_c extends Pointcut_c implements PCName
{
	protected Receiver target;
    protected String name;
    protected List args;
    protected MethodInstance mi;
    
    public PCName_c(Position pos, Receiver target,String name, List args)  {
	    super(pos);
	    this.target = target;
        this.name = name;
        this.args =  copyList(args); // here it is a list of TypeNode
    }

	private List copyList(List xs) {
		return new LinkedList(xs);
	}

    public Precedence precedence() {
	    return Precedence.LITERAL;
    }
    

	/** Get the target type of the pointcut reference. */
	public Receiver target() {
	  return this.target;
	}

	/** Set the target object of the pointcut reference. */
	public PCName target(Receiver target) {
	  PCName_c n = (PCName_c) copy();
	  n.target = target;
	  return n;
	}

	/** Get the name of the pointcut reference. */
	public String name() {
	  return this.name;
	}

	/** Set the name of the pointcut reference. */
	public PCName name(String name) {
	  PCName_c n = (PCName_c) copy();
	  n.name = name;
	  return n;
	}

	public ProcedureInstance procedureInstance() {
		return pointcutInstance();
	}

	/** Get the pointcut instance of the reference. */
	public MethodInstance pointcutInstance() {
	  return this.mi;
	}

	/** Set the pointcut instance of the reference. */
	public PCName pointcutInstance(MethodInstance mi) {
	  PCName_c n = (PCName_c) copy();
	  n.mi = mi;
	  return n;
	}

	/** Get the actual arguments of the reference. */
	public List arguments() {
	  return this.args;
	}

	/** Set the actual arguments of the reference. */
	public PCName arguments(List arguments) {
	  PCName_c n = (PCName_c) copy();
	  n.args = copyList(arguments);
	  return n;
	}

	/** Reconstruct the pointcut call. */
	 protected PCName_c reconstruct(Receiver target, List args) {
	   if (target != this.target || ! CollectionUtil.equals(args,this.args)) {
		 PCName_c n = (PCName_c) copy();
		 n.target = (target == null ? null : (Receiver) target.copy());
		 n.args =  copyList(args); // may become a list of TypeNode and Local
		 return n;
	   }
	   return this;
	 }
	 
	 /** Visit the children of the pointcut call. */
	 public Node visitChildren(NodeVisitor v) {
	 	Receiver target = (Receiver) visitChild(this.target,v);
	   List args = visitList(this.args, v);
	   return reconstruct(target,args);
	 }
	 
	 /** build the types */
	public Node buildTypes(TypeBuilder tb) throws SemanticException {
	   TypeSystem ts = tb.typeSystem();

	   List l = new ArrayList(args.size());
	   for (int i = 0; i < args.size(); i++) {
		 l.add(ts.unknownType(position()));
	   }

	   MethodInstance mi = ((AspectJTypeSystem)ts).pointcutInstance(position(), ts.Object(),
											 Flags.NONE,
											 ts.unknownType(position()),
											 name, l,
											 Collections.EMPTY_LIST);
	   return this.pointcutInstance(mi);
	 }
	 
	public ClassType findPointcutScope(Context_c c, String name) throws SemanticException {	
		   ClassType container = c.findMethodContainerInThisScope("$pointcut$"+name);

		   if (container != null) {
			   return container;
		   }

          Context_c outer = (Context_c) c.pop();
		   if (outer != null) {
			   return findPointcutScope(outer,name);
		   }

		   throw new SemanticException("Pointcut " + name + " not found.", position());
	   }

	/** type check the pointcut reference */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
	   TypeSystem ts = tc.typeSystem();
	   Context c = tc.context();

	   ReferenceType targetType = null;

	   /* By default, we're not in a static context.  But if the
		* target of the method is a type name, or if the target isn't
		* specified, and we're inside a static method, then we're
		* in a static context. */
	   boolean staticContext = false;

	   if (target instanceof TypeNode) {
		 	TypeNode tn = (TypeNode) target;
		 	Type t = tn.type();

		 	staticContext = true;

		 	if (t.isReference()) {
		   		targetType = t.toReference();
		 	} else {
		   		throw new SemanticException("Cannot reference pointcut  \"" + name
									   + "\" in non-reference type " + t + ".",
									   tn.position());
		 	}
	   } else if (target instanceof Expr) {
		       throw new SemanticException("Cannot reference pointcut \"" + name 
		                            + "\" in dynamic object \"" + target + "\".", 
		                            target.position());
	   } else if (target != null) {
		 throw new SemanticException("Host of pointcut reference must be a "
									 + "reference type.",
									 target.position());
	   } else { // target == null
		 CodeInstance ci = c.currentCode();
		 if (ci.flags().isStatic()) {
		   staticContext = true;
		 }
	   }

       // get list of argument types
	   List argTypes = new ArrayList(args.size());
	   for (Iterator i = args.iterator(); i.hasNext(); ) {
		 Object e =  i.next();
		 if (e instanceof Local)
		    argTypes.add(((Local)e).type());
		 else if (e instanceof TypeNode)
		    argTypes.add(((TypeNode)e).type());
	   }

       // find nearest enclosing pointcut declaration
	   MethodInstance mi;
       ClassType ct = findPointcutScope((Context_c)c,name);
       List ms = ct.methodsNamed("$pointcut$"+name);
       mi = (MethodInstance) ms.iterator().next(); // PointcutInstance_c
      
       // get the formal types
       List formalTypes = mi.formalTypes();
       
       // check the arguments, NullType matches anything
       if (argTypes.size() != formalTypes.size()) {
       		throw new SemanticException("Wrong number of arguments to pointcut," +
       		                                                         " expected " + formalTypes.size(), position());
       }
       Iterator a = argTypes.iterator();
       for (Iterator b = formalTypes.iterator(); b.hasNext();  ) {
       	    Type argtype = (Type)a.next();
       	    Type formaltype = (Type)b.next();
       	    if (! (argtype instanceof NullType)) 
       	        if (!ts.isImplicitCastValid(argtype,formaltype))
       	        	throw new SemanticException("Wrong argument type "+argtype+
       	        	                                                        " expected " + formaltype,argtype.position());
       }
      
	   return this.pointcutInstance(mi);
	 }	
	 
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write(name+"(");
        for (Iterator i = args.iterator(); i.hasNext(); ) {
	        FormalPattern id = (FormalPattern) i.next();
                print(id,w,tr);
		
		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
        w.write(")");
    }

}

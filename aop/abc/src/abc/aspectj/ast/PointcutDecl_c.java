package arc.aspectj.ast;

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

import arc.aspectj.ast.Pointcut;
import arc.aspectj.types.AspectJTypeSystem;

public class PointcutDecl_c extends MethodDecl_c implements PointcutDecl
{
    String name;
    Pointcut pc;

	private static List adviceFormals(List formals) {
		  List result = new TypedList(new LinkedList(), AdviceFormal.class, false);
		  for (Iterator i = formals.iterator(); i.hasNext(); ) {
			  Formal f = (Formal) i.next();
			  result.add(new AdviceFormal_c(f));
		  }
		  return result;
	  }
	  
    public PointcutDecl_c(Position pos,
                          Flags flags,
                          String name,
                          List formals,
                          Pointcut pc,
                          TypeNode voidn)
    {  super(pos,
			  flags, 
			  voidn,
			  "$pointcut$"+name,
			  adviceFormals(formals),
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

				MethodInstance mi = ((AspectJTypeSystem)ts).pointcutInstance(position(), ts.Object(),
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
				flags = flags.Public().Abstract();
			}
		
			return ((AspectJTypeSystem)ts).pointcutInstance(position(),
										   ct, flags, returnType.type(), name,
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

	   if (pc == null && ! (flags().isAbstract() || flags().isNative())) {
		   throw new SemanticException("Missing pointcut body.", position());
	   }

	   if (pc != null && flags().isAbstract()) {
		   throw new SemanticException(
		   "An abstract pointcut cannot have a body.", position());
	   }
		   overrideMethodCheck(tc);

	  if (pc != null)
	  	pc.checkFormals(formals);
	  	
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
    
}







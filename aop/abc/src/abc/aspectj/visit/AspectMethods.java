
package abc.aspectj.visit;

import java.util.List;
import java.util.Stack;
import java.util.Iterator;
import java.util.LinkedList;

import polyglot.ast.Node;
import polyglot.ast.MethodDecl;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ClassDecl;
import polyglot.ast.NodeFactory;
import polyglot.ast.Local;
import polyglot.ast.Field;
import polyglot.ast.Call;
import polyglot.ast.New;

import polyglot.visit.NodeVisitor;

import polyglot.types.TypeSystem;
import polyglot.types.ParsedClassType;

import abc.aspectj.ast.PCIf;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AspectDecl;
import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.ast.PointcutDecl;
import abc.aspectj.ast.ProceedCall;

import abc.aspectj.ast.IntertypeFieldDecl;
import abc.aspectj.ast.IntertypeFieldDecl_c;
import abc.aspectj.ast.IntertypeMethodDecl_c;
import abc.aspectj.ast.IntertypeConstructorDecl_c;

import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.InterTypeConstructorInstance_c;

public class AspectMethods extends NodeVisitor {

    private Stack /* List MethodDecl */ methods; // method declaration lists, one for each classdecl
    private Stack /* List Formal */ formals; // pointcut formals for generating if-methods
    private Stack /* List MethodDecl */ proceeds; // dummy proceed methods for transforming proceed calls
    private Stack /* List AdviceDecl */ advices;
    private Stack /* ParsedClassType */ container; // Keep track of current container
    
	public AspectJNodeFactory nf;
	public AspectJTypeSystem ts;
	
	public AspectMethods(NodeFactory nf, TypeSystem ts) {
		super();
		this.nf = (AspectJNodeFactory) nf;
		this.ts = (AspectJTypeSystem) ts;
		this.methods = new Stack();
		this.formals = new Stack();
		this.proceeds = new Stack();
		this.advices = new Stack();
		this.container = new Stack();
	}
	
	public NodeVisitor enter(Node n) {
	    if (n instanceof ClassDecl) {
		methods.push(new LinkedList());
		container.push(((ClassDecl)n).type());
	    }
		if (n instanceof PointcutDecl) {  
			PointcutDecl pd = (PointcutDecl) n;
			formals.push(pd.formals());
		}
		if (n instanceof AdviceDecl) {
			AdviceDecl ad = (AdviceDecl) n;
			MethodDecl md = ad.proceedDecl(nf,ts);
			proceeds.push(md);
			formals.push(ad.formals());
			advices.push(ad);
		}
		return this;
	 }
	 
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
 
/* intertype declarations: */
		if (n instanceof Field) {
			Field f = (Field) n;
			if (f.fieldInstance() instanceof InterTypeFieldInstance_c) {
				InterTypeFieldInstance_c itfi = (InterTypeFieldInstance_c) f.fieldInstance();
				return f.fieldInstance(itfi.mangled()).name(itfi.mangled().name());
			}
			return n;
		}
		if (n instanceof IntertypeFieldDecl_c) {
			IntertypeFieldDecl_c itfd = (IntertypeFieldDecl_c) n;
			return itfd.accessChange(); // mangle name if private
		}
		if (n instanceof Call) {
			Call c = (Call) n;
			if (c.methodInstance() instanceof InterTypeMethodInstance_c) {
				InterTypeMethodInstance_c itmi = (InterTypeMethodInstance_c) c.methodInstance();
				return c.methodInstance(itmi.mangled()).name(itmi.mangled().name());
			}
		}
		if (n instanceof IntertypeMethodDecl_c) {
			IntertypeMethodDecl_c itmd = (IntertypeMethodDecl_c) n;
			return itmd.accessChange();
		}
		if (n instanceof ConstructorCall) {
				ConstructorCall cc = (ConstructorCall) n;
				System.out.println(cc);
				if (cc.constructorInstance() instanceof InterTypeConstructorInstance_c) {
					InterTypeConstructorInstance_c itcd = (InterTypeConstructorInstance_c) cc.constructorInstance();
					return itcd.mangledCall(cc,nf,ts);
				}
			}
		if (n instanceof New) {
				New cc = (New) n;
				System.out.println(cc);
				if (cc.constructorInstance() instanceof InterTypeConstructorInstance_c) {
					InterTypeConstructorInstance_c itcd = (InterTypeConstructorInstance_c) cc.constructorInstance();
					return itcd.mangledNew(cc,nf,ts);
			}
		}
		if (n instanceof IntertypeConstructorDecl_c) {
			IntertypeConstructorDecl_c itcd = (IntertypeConstructorDecl_c) n;
			return itcd.accessChange(nf,ts);
		}
/* advice: */
		if (n instanceof AdviceDecl) {
			formals.pop();
			advices.pop();
			MethodDecl md = (MethodDecl) proceeds.pop(); // returns null if not around
			if (md != null) 
			  ((List)methods.peek()).add(md); // record the dummy proceed method
			AdviceDecl ad = (AdviceDecl) n;
			return ad.methodDecl(nf,ts);  // turn advice into ordinary method
		}
		if (n instanceof ProceedCall) {
			ProceedCall pc = (ProceedCall) n.copy(); 
			return pc.proceedMethod((MethodDecl) proceeds.peek()); // replace by call to dummy proceed method
		}
		if (! (advices.isEmpty()) && n instanceof Local) { 
				Local m = (Local) n;
				AdviceDecl currentAdvice = (AdviceDecl) advices.peek();
				currentAdvice.joinpointFormals(m); // add joinpoint formals where necessary
				return n;
		}
		if (n instanceof PCIf) {  // lift expression in if-pointcut to method
			PCIf ifpcd = (PCIf) n;
			MethodDecl md = ifpcd.exprMethod(nf,ts,(List) formals.peek(), (ParsedClassType) container.peek()); // construct method for expression in if(..)
			((List)methods.peek()).add(md);
			return ifpcd.liftMethod(nf); // replace expression by method call
		}
/* add new methods to the class */
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n.copy();
			container.pop();
			List localMethods = (List) methods.pop();
			for (Iterator i = localMethods.iterator(); i.hasNext(); ) {
				MethodDecl md = (MethodDecl) i.next();
				cd = cd.body(cd.body().addMember(md));
			}
			return cd;
		}
	    return super.leave(old, n, v);
}
}

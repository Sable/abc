
package abc.aspectj.ast;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import polyglot.util.Position;

import polyglot.ast.Node;
import polyglot.ast.Expr;
import polyglot.ast.MethodDecl;
import polyglot.ast.Receiver;
import polyglot.ast.Special;
import polyglot.ast.Call;
import polyglot.ast.TypeNode;
import polyglot.ext.jl.ast.Call_c;

import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.CodeInstance;

import polyglot.visit.TypeChecker;
import polyglot.visit.TypeBuilder;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.ast.HostSpecial_c;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.visit.AspectMethods;


public class ProceedCall_c extends Call_c
                           implements ProceedCall, MakesAspectMethods
{
	private MethodDecl proceedDecl;
	private CodeInstance ci;
	
	public ProceedCall_c(Position pos, Receiver recv, List arguments) {
		super(pos,recv,"proceed",arguments);		         	
    }
    
    public ProceedCall_c(Call c) {
    	super(c.position(),c.target(),c.name(),c.arguments());
    }
    
    public ProceedCall proceedMethod(MethodDecl md,AdviceDecl ad) {
    	ad.proceedContainer(ci);
    	return (ProceedCall) name(md.name()).methodInstance(md.methodInstance());
    }
    
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		
		    TypeSystem ts = tc.typeSystem();
		    Context c = tc.context();
		    
		    // register the containing method for later use
		    ci = c.currentCode();
		    
		    // check whether we are in the scope of an advice declaration,
		    // and retrieve proceed's intended type
		    MethodInstance mi = AdviceDecl_c.proceedInstance(c);
			if (mi==null)
			     throw new SemanticException ("proceed can only be used in around advice");
			 
			 // collect types of the actual arguments    
			List argTypes = new ArrayList(arguments.size());
			for (Iterator i = arguments.iterator(); i.hasNext(); ) {
				 Expr e = (Expr) i.next();
				 argTypes.add(e.type());
			}

            // match actuals against formals
			if (! mi.callValid(argTypes))
			   throw new SemanticException ("proceed arguments "+argTypes+
                                            " do not match advice formals "+mi.formalTypes());
             
            TypeNode tn = tc.nodeFactory().CanonicalTypeNode(position(),mi.container());
                                                                
             // rewrite the call                                                   
			return this.methodInstance(mi).target(tn).type(mi.returnType());
	}
	
        public void aspectMethodsEnter(AspectMethods visitor)
        {
                // do nothing       
                // visitor.advice().addProceedCall(this.)
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf,
                                       AspectJTypeSystem ts)
        {
        		ProceedCall pc = (ProceedCall) this.copy();
                return pc.proceedMethod(visitor.proceed(),visitor.advice());
        }
        
        
}

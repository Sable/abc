
package abc.aspectj.ast;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import polyglot.util.Position;

import polyglot.ast.Node;
import polyglot.ast.Expr;
import polyglot.ast.MethodDecl;
import polyglot.ast.Receiver;
import polyglot.ast.Call;
import polyglot.ext.jl.ast.Call_c;

import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;

import polyglot.visit.TypeChecker;
import polyglot.visit.TypeBuilder;


public class ProceedCall_c extends Call_c implements ProceedCall {

	private MethodDecl proceedDecl;
	
	public ProceedCall_c(Position pos, List arguments) {
		super(pos,null,"proceed",arguments);		         	
    }
    
    public ProceedCall_c(Call c) {
    	super(c.position(),c.target(),c.name(),c.arguments());
    }
    
    public ProceedCall proceedMethod(MethodDecl md) {
    	return (ProceedCall) name(md.name());
    }
    
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		
		    TypeSystem ts = tc.typeSystem();
		    Context c = tc.context();
		    
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
                                                                
             // rewrite the call                                                   
			return this.methodInstance(mi).type(mi.returnType());
	}
	
	

}

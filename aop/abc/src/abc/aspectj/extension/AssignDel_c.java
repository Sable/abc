package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;

import polyglot.util.InternalCompilerError;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;
import polyglot.types.*;
import polyglot.visit.*;

public class AssignDel_c extends JL_c
{
	
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		AJContext ajc = (AJContext) tc.context();
		Assign a = (Assign) node();
		if (ajc.inIf() && (a.left() instanceof Local))
			throw new SemanticException("Cannot assign to a local within a pointcut.", node().position());
		return node().typeCheck(tc);
	}
}


package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Term_c;

import abc.aspectj.types.AJContext;

/** base class for "declare X" declarations.
 * 
 * @author Oege de Moor
 */
public class DeclareDecl_c extends Term_c implements DeclareDecl
{

    public DeclareDecl_c(Position pos) {
        super(pos);
    }
    
	public List acceptCFG(CFGBuilder v, List succs) {
		   return succs;
	}
	
	public Term entry() {
		return this;
	}
	
	public Context enterScope(Context c) {
		return ((AJContext)c).pushDeclare();
	}

}

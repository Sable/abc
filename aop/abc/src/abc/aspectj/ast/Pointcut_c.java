package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

/**
 * 
 * @author Oege de Moor
 *
 */
public abstract class Pointcut_c extends Node_c implements Pointcut
{

    public Pointcut_c(Position pos) {
        super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNKNOWN;
    }

    public void printSubExpr(Pointcut pc, boolean associative,
                             CodeWriter w, PrettyPrinter pp) {
        if (! associative && precedence().equals(pc.precedence()) ||
	    precedence().isTighter(pc.precedence())) {
	    w.write("(");
            printBlock(pc, w, pp);
	    w.write( ")");
	}
        else {
            printBlock(pc, w, pp);
        }
    }
    
   public Collection mayBind()  throws SemanticException {
   	    return new HashSet();
   }
   
   public Collection mustBind() {
   		return new HashSet();
   }
  
   public static String initialised; 
   
   public void checkFormals(List formals) throws SemanticException {
   		
   		Collection maybind = mayBind();   // check for repeated bindings
   		
   		// now look for undefined formals
   		Collection mustbind = mustBind();
   		
	 	for (Iterator nb = formals.iterator(); nb.hasNext(); ) {
			   Formal l = (Formal) nb.next();
			   if (!(mustbind.contains(l.name())))
			   	   throw new SemanticException("Formal \""+ l.name() + "\" may be unbound in pointcut.",
																		l.position());
		}
   }

}


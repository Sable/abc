package abc.aspectj.extension;

import polyglot.util.CodeWriter;
import polyglot.ast.*;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.visit.TypeChecker;
import polyglot.visit.PrettyPrinter;
import polyglot.ext.jl.ast.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

/**
 * @author Julian Tibble
 * @author Oege de Moor
 *
 */
public class LocalDel_c extends JL_c implements MakesAspectMethods,
                                                TransformsAspectReflection
{
   
     
     public Node typeCheck(TypeChecker tc) throws SemanticException {
     	 AJContext ajc =(AJContext) tc.context();
     	 if (ajc.inCflow() && ajc.inIf()) {
     	 	Local m = (Local) node();
     	 	if (! ajc.getCflowMustBind().contains(m.name()))
     	 		throw new SemanticException("Local "+m.name()+" is not bound within enclosing cflow: it cannot be used within if(..)",node().position());
     	 }
     	 return node().typeCheck(tc);
     } 
        
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // do nothing
    }
    
    
    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        if (visitor.isAdvice()) {
            Local m = (Local) node();
            AdviceDecl currentAdvice = visitor.advice();

            // add joinpoint formals where necessary
            currentAdvice.joinpointFormals(m);
        }

        if (visitor.isPCIf()) {
            Local m = (Local) node();
            PCIf currentPCIf = visitor.pcif();

            // add joinpoint formals where necessary
            currentPCIf.joinpointFormals(m);
        }

        return node();
    }

    public void enterAspectReflectionInspect(AspectReflectionInspect v,Node parent) {
	if(!v.inspectingLocals()) return;

	Local m=(Local) node();

	if(!m.name().equals("thisJoinPoint")) return;

	if(parent instanceof Call) {
	    String name=((Call) parent).name();

	    if(name.equals("getKind") || 
	       name.equals("getSignature") ||
	       name.equals("getSourceLocation") ||
	       name.equals("toShortString") ||
	       name.equals("toLongString") ||
	       name.equals("toString")) return;
	}
	v.disableTransform();
    }

    public void leaveAspectReflectionInspect(AspectReflectionInspect v) {
    }

    public void enterAspectReflectionRewrite(AspectReflectionRewrite v, AJTypeSystem ts) {
    }

    public Node leaveAspectReflectionRewrite(AspectReflectionRewrite v, AJNodeFactory nf) {
	if(!v.inspectingLocals()) return node();

	Local m=(Local) node();
	if(m.name().equals("thisJoinPoint")) {
	    LocalInstance li=v.getJPSP();
	    if(li!=null)
		return nf.Local(m.position(),"thisJoinPointStaticPart")
		    .localInstance(li)
		    .type(li.type());
	}
	return node();
    }
}


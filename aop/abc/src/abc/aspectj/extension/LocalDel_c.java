package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.types.LocalInstance;
import polyglot.ext.jl.ast.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

public class LocalDel_c extends JL_c implements MakesAspectMethods,
                                                TransformsAspectReflection
{
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // do nothing
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf,
                                   AspectJTypeSystem ts)
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

    public void enterAspectReflectionRewrite(AspectReflectionRewrite v,AspectJTypeSystem ts) {
    }

    public Node leaveAspectReflectionRewrite(AspectReflectionRewrite v,AspectJNodeFactory nf) {
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


package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

public class LocalDel_c extends JL_c implements MakesAspectMethods
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
}


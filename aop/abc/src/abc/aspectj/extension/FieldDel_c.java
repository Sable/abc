package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

public class FieldDel_c extends JL_c implements MakesAspectMethods
{
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // do nothing
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf,
                                   AspectJTypeSystem ts)
    {
        Field f = (Field) node();

        if (f.fieldInstance() instanceof InterTypeFieldInstance_c) {
            InterTypeFieldInstance_c itfi = (InterTypeFieldInstance_c) f.fieldInstance();
            if (itfi.container().toClass().flags().isInterface())
                return itfi.getCall(nf, ts, f.target(), itfi.container());
            f = f.fieldInstance(itfi.mangled()).name(itfi.mangled().name()).targetImplicit(false);
        }

        if (f.target() instanceof HostSpecial_c)
        {
            HostSpecial_c hs = (HostSpecial_c) f.target();
            if (hs.kind() == Special.SUPER) {
                IntertypeDecl id = (IntertypeDecl) visitor.intertypeDecl();
                return id.getSupers().superFieldGetter(nf, ts, f, id.host().type().toClass(),
                                                                  id.thisReference(nf, ts));
            }
        }
        return f;
    }
}


package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;

import polyglot.util.InternalCompilerError;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;

public class AssignDel_c extends JL_c implements MakesAspectMethods
{
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        visitor.pushLhs(((Assign) node()).left());
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        Node n = node();
        Expr oldleft = visitor.lhs();
        visitor.popLhs();

        if (oldleft instanceof Field) {
            Field fieldleft = (Field) oldleft;
            if (fieldleft.fieldInstance() instanceof InterTypeFieldInstance_c) {
                InterTypeFieldInstance_c itfi = (InterTypeFieldInstance_c) fieldleft.fieldInstance();
                if (itfi.container().toClass().flags().isInterface()) {
                    Assign a = (Assign) n;
                    Receiver target = null;
                    if (a.left() instanceof Field)
                        target = ((Field) a.left()).target();
                    if (a.left() instanceof Call)
                        target = ((Call) a.left()).target();
                    if (target == null)
                        throw new InternalCompilerError("reference to intertype field with receiver that is not a call or a field");
                    return itfi.setCall(nf,ts,target,itfi.container(),a.right());
                }
            }
            if (fieldleft.target() instanceof HostSpecial_c) {
                HostSpecial_c hs = (HostSpecial_c) fieldleft.target();
                if (hs.kind() == Special.SUPER) {
                    IntertypeDecl id = visitor.intertypeDecl();
                    return id.getSupers().superFieldSetter(nf, ts,
                                    fieldleft,id.host().type().toClass(),id.thisReference(nf,ts),
                                    ((Assign) n).right());
                }
            }
        }
        return n;
    }
}


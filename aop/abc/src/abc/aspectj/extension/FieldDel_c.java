package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.util.InternalCompilerError;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;
import polyglot.types.ClassType;

public class FieldDel_c extends JL_c implements MakesAspectMethods
{
    /** Indicates that whether accessor methods for fields should be introduced. Intended for use in
     * combination with Unary - it is set in aspectMethodsEnter of UnaryDel_c for Fields that are
     * operands of ++/--, i.e. will need to be used both as L-values and as R-values.
     */
    boolean introduceFieldAccessors = true;
    
    public void aspectMethodsEnter(AspectMethods visitor)
    {
        // do nothing
    }

    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                   AJTypeSystem ts)
    {
        Field f = (Field) node();

        if(ts.isAccessible(f.fieldInstance(), visitor.context()) && 
                !ts.isAccessibleIgnorePrivileged(f.fieldInstance(), visitor.context()) &&
                introduceFieldAccessors) {
       	    ClassType cct = (ClassType) visitor.container(); // TODO: Check container() is what we want
    	    while(cct != null) {
    	        if(AJFlags.isPrivilegedaspect(cct.flags())) {
    	            AspectType at = (AspectType) cct;
    	            return at.getAccessorMethods().accessorGetter(nf, ts, f, (ClassType)f.target().type(), null);
    	        }
    	        cct = cct.outer();
    	    }
    	    // Shouldn't happen - accessibility test thinks we're in a privileged aspect, 
    	    // but we failed to find a containing aspect
    	    throw new InternalCompilerError("Problem determining whether or not we're in a privileged aspect");
        }
        
        if (f.fieldInstance() instanceof InterTypeFieldInstance_c) {
            InterTypeFieldInstance_c itfi = (InterTypeFieldInstance_c) f.fieldInstance();
            if (itfi.container().toClass().flags().isInterface())
                return itfi.getCall(nf, ts, f.target(), itfi.container());
            f = f.fieldInstance(itfi.mangled()).name(itfi.mangled().name()).targetImplicit(false);
        }

        if (f.target() instanceof HostSpecial_c && introduceFieldAccessors)
        {
            HostSpecial_c hs = (HostSpecial_c) f.target();
            if (hs.kind() == Special.SUPER) {
                IntertypeDecl id = (IntertypeDecl) visitor.intertypeDecl();
                /*return id.getSupers().superFieldGetter(nf, ts, f, id.host().type().toClass(),
                                                                  id.thisReference(nf, ts));*/
        	    AspectType aspect = ((AJContext)visitor.context()).currentAspect();
        	    if(aspect == null) {
        	        // Is this really impossible? Depends on how exactly the nesting works, investigate
        	        throw new InternalCompilerError("Intertype method not enclosed by aspect");
        	    }
        	    return aspect.getAccessorMethods().accessorGetter(nf, ts, f, id.host().type().toClass(),
        	            	id.thisReference(nf, ts));
            }
        }
        return f;
    }
}


package abc.weaving.aspectinfo;

import soot.SootMethod;
import polyglot.types.MethodInstance;
import polyglot.types.ClassType;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import java.util.ArrayList;
import soot.Modifier;

/**
 * @author Pavel Avgustinov
 */
public class AccessorQualSpecial extends AccessorMethod {
    MethodInstance mi;
    ClassType qualifier;
    boolean qualThisNotSuper;
    
    public AccessorQualSpecial(String name, MethodInstance mi, ClassType target,
            		ClassType qualifier, Position pos, boolean qualThisNotSuper) {
        super(name, target, pos);
        this.mi = mi;
        this.qualifier = qualifier;
        this.qualThisNotSuper = qualThisNotSuper;
        if(qualThisNotSuper == false) {
            throw new InternalCompilerError("Qualified super access not yet implemented");
        }
    }

    /* (non-Javadoc)
     * @see abc.weaving.aspectinfo.AccessorMethod#addSootMethod(int)
     */
    public void addSootMethod(int modifiers) {
        MethodSig method = AbcFactory.MethodSig(mi);
        ArrayList paramTypes = new ArrayList();
        soot.Type retType = method.getReturnType().getSootType();
        
        // Ignore modifiers - this must be public
        modifiers = Modifier.PUBLIC;
        
        soot.SootClass sc = AbcFactory.AbcClass(target).getSootClass();
        
        SootMethod sm = new SootMethod(method.getName(), paramTypes, retType, modifiers);
        sc.addMethod(sm);
        
        registerMethod(sm);
    }

    /* (non-Javadoc)
     * @see abc.weaving.aspectinfo.AccessorMethod#registerMethod(soot.SootMethod)
     */
    public void registerMethod(SootMethod sm) {
        if(qualThisNotSuper) {
            MethodCategory.register(sm, MethodCategory.THIS_GET);
        }
    }

}

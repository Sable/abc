package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

public class IntertypeAdjuster {
    public void adjust() {
        // Generate Soot signatures for intertype methods and fields
        for( Iterator imdIt = GlobalAspectInfo.v().getIntertypeMethodDecls().iterator(); imdIt.hasNext(); ) {
            final IntertypeMethodDecl imd = (IntertypeMethodDecl) imdIt.next();
            addMethod( imd );
        }
        for( Iterator ifdIt = GlobalAspectInfo.v().getIntertypeFieldDecls().iterator(); ifdIt.hasNext(); ) {
            final IntertypeFieldDecl ifd = (IntertypeFieldDecl) ifdIt.next();
            addField( ifd );
        }
    }

    private void addMethod( IntertypeMethodDecl imd ) {
        MethodSig method = imd.getTarget();

        SootClass sc = method.getDeclaringClass().getSootClass();
        Type retType = method.getReturnType().getSootType();
        List parms = new ArrayList();
        for( Iterator parmTypeIt = method.getParams().iterator(); parmTypeIt.hasNext(); ) {
            final AbcType parmType = (AbcType) parmTypeIt.next();
            parms.add(parmType.getSootType());
        }

        int modifiers = method.getModifiers();
        modifiers |= Modifier.PUBLIC;
        modifiers &= ~Modifier.PRIVATE;
        modifiers &= ~Modifier.PROTECTED;
            
        // Create the method
        SootMethod sm = new SootMethod( 
                method.getName(),
                parms,
                retType,
                modifiers );

        for( Iterator exceptionIt = method.getExceptions().iterator(); exceptionIt.hasNext(); ) {

            final SootClass exception = (SootClass) exceptionIt.next();
            sm.addException( exception );
        }

        // Fool Soot into generating Jimple for this method from 
        // implementation dummy method code
        sm.setSource( imd.getImpl().getSootMethod().getSource() );

        // Add it to the class
        sc.addMethod(sm);
    }

    private void addField( IntertypeFieldDecl ifd ) {
        FieldSig field = ifd.getTarget();

        int modifiers = field.getModifiers();
        modifiers |= Modifier.PUBLIC;
        modifiers &= ~Modifier.PRIVATE;
        modifiers &= ~Modifier.PROTECTED;

        SootClass cl = field.getDeclaringClass().getSootClass();
        if( cl.isInterface() ) {
            for( Iterator childClassIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); childClassIt.hasNext(); ) {
                final SootClass childClass = (SootClass) childClassIt.next();
                if( childClass.isInterface() ) continue;
                if( !implementsInterface(childClass, cl) ) continue;
                if( childClass.hasSuperclass() 
                && implementsInterface(childClass.getSuperclass(), cl) )
                    continue;

                // Add the field itself
                SootField newField = new SootField(
                        field.getName(),
                        field.getType().getSootType(),
                        modifiers );
                childClass.addField(newField);

            // TODO: add accessor methods

            }
        } else {
            // Add the field itself
            SootField newField = new SootField(
                    field.getName(),
                    field.getType().getSootType(),
                    modifiers );
            cl.addField(newField);
        }

        // TODO: Add dispatch methods
    }

    private boolean implementsInterface( SootClass child, SootClass iface ) {
        while(true) {
            if( child.getInterfaces().contains( iface ) ) return true;
            if( !child.hasSuperclass() ) return false;
            SootClass superClass = child.getSuperclass();
            if( superClass == child ) throw new RuntimeException( "Error: cycle in class hierarchy" );
            child = superClass;
        }
    }

}

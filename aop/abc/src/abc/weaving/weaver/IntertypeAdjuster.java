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

        SootClass cl = field.getDeclaringClass().getSootClass();
        if( cl.isInterface() ) {
            // TODO: deal with interfaces 
            throw new RuntimeException( "NYI" );
        } else {
            int modifiers = field.getModifiers();
            modifiers |= Modifier.PUBLIC;
            modifiers &= ~Modifier.PRIVATE;
            modifiers &= ~Modifier.PROTECTED;
            
            // Add the field itself
            SootField newField = new SootField(
                    field.getName(),
                    field.getType().getSootType(),
                    modifiers );
            field.getDeclaringClass().getSootClass().addField(newField);
        }

        // TODO: Add dispatch methods
    }

}

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

    // TODO: When Aske gives us modifiers, use them
    // TODO: When Aske gives us a throws list, use it
    private void addMethod( IntertypeMethodDecl imd ) {
        MethodSig method = imd.getTarget();

        SootClass sc = method.getDeclaringClass().getSootClass();
        Type retType = method.getReturnType().getSootType();
        List parms = new ArrayList();
        for( Iterator parmTypeIt = method.getParams().iterator(); parmTypeIt.hasNext(); ) {
            final AbcType parmType = (AbcType) parmTypeIt.next();
            parms.add(parmType.getSootType());
        }

        // Create the method
        SootMethod sm = new SootMethod( 
                method.getName(),
                parms,
                retType,
                Modifier.PUBLIC );

        // Fool Soot into generating Jimple for this method from 
        // implementation dummy method code
        sm.setSource( imd.getImpl().getSootMethod().getSource() );

        // Add it to the class
        sc.addMethod(sm);
    }

    // TODO: When Aske gives us modifiers, use them
    private void addField( IntertypeFieldDecl ifd ) {
        FieldSig field = ifd.getTarget();

        SootClass cl = field.getDeclaringClass().getSootClass();
        if( cl.isInterface() ) {
            // TODO: deal with interfaces 
            throw new RuntimeException( "NYI" );
        } else {
            // Add the field itself
            SootField newField = new SootField(
                    field.getName(),
                    field.getType().getSootType(),
                    Modifier.PUBLIC );
            field.getDeclaringClass().getSootClass().addField(newField);
        }

        // TODO: Add dispatch methods
    }

}

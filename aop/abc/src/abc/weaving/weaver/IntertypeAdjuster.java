package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;

import com.sun.rsasign.s;

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
		SootMethod implMethod = addImplMethod(imd);
		addTargetMethod(imd,implMethod);
	}
	
	private SootMethod addImplMethod( IntertypeMethodDecl imd ) {
		MethodSig method = imd.getImpl();
        
		SootClass sc = method.getDeclaringClass().getSootClass();
		Type retType = method.getReturnType().getSootType();
		List parms = new ArrayList();
		for( Iterator formalIt = method.getFormals().iterator(); formalIt.hasNext(); ) {
			final AbcType formalType = ((Formal) formalIt.next()).getType();
			parms.add(formalType.getSootType());
		}

		int modifiers = method.getModifiers();
		modifiers |= Modifier.PUBLIC;
		modifiers |= Modifier.STATIC; // the originating method is static
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
		
		sm.setSource(method.getSootMethod().getSource());
		
		sc.addMethod(sm);
		
		return sm;
	}
	
    private void addTargetMethod( IntertypeMethodDecl imd, SootMethod implMethod) {
        MethodSig method = imd.getTarget();
        
        SootClass sc = method.getDeclaringClass().getSootClass();
        Type retType = method.getReturnType().getSootType();
        List parms = new ArrayList();
        for( Iterator formalIt = method.getFormals().iterator(); formalIt.hasNext(); ) {
            final AbcType formalType = ((Formal) formalIt.next()).getType();
            parms.add(formalType.getSootType());
        }
        parms.remove(0); // drop the "this" parameter

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

		
/* generate call to implementation: impl(this,arg1,arg2,...,argn) */	
    //create a body
	 	Body b = Jimple.v().newBody(sm); sm.setActiveBody(b);
	 	Chain ls = b.getLocals();
	 	PatchingChain ss = b.getUnits();
    // argument set-up
	    List args = new LinkedList();
    //	the first parameter of the impl is "this : TargetType"
		RefType rt = sc.getType(); 
		ThisRef thisref = Jimple.v().newThisRef(rt); 
		Local v = Jimple.v().newLocal("this$loc",rt); ; ls.add(v);
		IdentityStmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(v, thisref); ss.add(thisStmt);
		args.add(v);
	// add references to the other parameters
		int index = 0;
		for (Iterator formals=parms.iterator(); formals.hasNext(); ) {
			final AbcType formalType = ((Formal) formals.next()).getType();
			Local p = Jimple.v().newLocal("param$"+index,rt); ; ls.add(v);
			ParameterRef pr = Jimple.v().newParameterRef(formalType.getSootType(),index);
			IdentityStmt prStmt = soot.jimple.Jimple.v().newIdentityStmt(p, thisref); ss.add(prStmt);
			args.add(p);
			index++;
		}
	// now invoke the implementation in the originating aspect
		InvokeExpr ie = Jimple.v().newStaticInvokeExpr(implMethod,args);
	// if this is a void returntype, create call followed by return
	// otherwise return the value directly
		if (retType.equals(VoidType.v())) {
			InvokeStmt stmt1 = Jimple.v().newInvokeStmt(ie);
			ReturnVoidStmt stmt2 = Jimple.v().newReturnVoidStmt();
			ss.add(stmt1); ss.add(stmt2);
		} else {
			ReturnStmt stmt = Jimple.v().newReturnStmt(ie);
			ss.add(stmt);
		}
    // Add method to the class
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

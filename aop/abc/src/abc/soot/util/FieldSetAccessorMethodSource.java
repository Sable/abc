/*
 * Created on Jul 27, 2004
 */
package abc.soot.util;

/** Abc-specific generalisation of soot.javaToJimple.PrivateFieldSetMethodSource
 * 
 * @author pavel
 */
import java.util.*;

import soot.javaToJimple.LocalGenerator;
import soot.jimple.Jimple;

public class FieldSetAccessorMethodSource implements soot.MethodSource {

//    private polyglot.types.FieldInstance fieldInst;
    private soot.Type fieldType;
    private String fieldName;
    private soot.SootClass receiver;
    private boolean isStatic;
    
    public FieldSetAccessorMethodSource(soot.Type fieldType, String fieldName, soot.SootClass receiver, boolean isStatic) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.receiver = receiver;
        this.isStatic = isStatic;
    }
    
/*    public void fieldName(String n){
        fieldName = n;
    }
    
    public void fieldType(soot.Type type){
        fieldType = type;
    }*/
        
    /*public void setFieldInst(polyglot.types.FieldInstance fi) {
        fieldInst = fi;
    }*/
    
    public soot.Body getBody(soot.SootMethod sootMethod, String phaseName){
        
        soot.Body body = soot.jimple.Jimple.v().newBody(sootMethod);
        LocalGenerator lg = new LocalGenerator(body);
        
        soot.Local fieldBase = lg.generateLocal(receiver.getType());
        
        soot.Local assignLocal = null;
        // create parameters
        int paramCounter = 0;
        Iterator paramIt = sootMethod.getParameterTypes().iterator();
        while (paramIt.hasNext()) {
            soot.Type sootType = (soot.Type)paramIt.next();
            soot.Local paramLocal = lg.generateLocal(sootType);
            
            soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v().newParameterRef(sootType, paramCounter);
            soot.jimple.Stmt stmt = soot.jimple.Jimple.v().newIdentityStmt(paramLocal, paramRef);
            body.getUnits().add(stmt);
            
            assignLocal = paramLocal;
            paramCounter++;
        }
        
        // create field type local
        //soot.Local fieldLocal = lg.generateLocal(fieldType);
        // assign local to fieldRef
        soot.SootFieldRef field = soot.Scene.v().makeFieldRef(receiver, fieldName, fieldType, isStatic);

        soot.jimple.FieldRef fieldRef = null;
        if (isStatic) {
            fieldRef = soot.jimple.Jimple.v().newStaticFieldRef(field);
        }
        else {
            fieldRef = soot.jimple.Jimple.v().newInstanceFieldRef(fieldBase, field);
            body.getUnits().add(soot.jimple.Jimple.v().newIdentityStmt(fieldBase, Jimple.v().newThisRef(receiver.getType())));
        }
        //System.out.println("fieldRef: "+fieldRef+" assignLocal: "+assignLocal);
        soot.jimple.AssignStmt assign = soot.jimple.Jimple.v().newAssignStmt(fieldRef, assignLocal);
        body.getUnits().add(assign);

        //return local
        soot.jimple.Stmt retStmt = soot.jimple.Jimple.v().newReturnStmt(assignLocal);
        body.getUnits().add(retStmt);
        
        return body;
     
    }
}

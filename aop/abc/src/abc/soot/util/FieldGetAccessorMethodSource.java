/*
 * Created on Jul 27, 2004
 */
package abc.soot.util;

/** Abc-specific generalisation of soot.javaToJimple.PrivateFieldAccMethodSource
 * 
 * @author pavel
 */

import java.util.*;

import soot.javaToJimple.LocalGenerator;
import soot.jimple.Jimple;

public class FieldGetAccessorMethodSource implements soot.MethodSource {
//  private polyglot.types.FieldInstance fieldInst;
    private soot.Type fieldType;
    private String fieldName;
    private soot.SootClass receiver;

    public FieldGetAccessorMethodSource(soot.Type fieldType, String fieldName, soot.SootClass receiver) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.receiver = receiver;
    }
    
        
/*        public void fieldName(String n){
            fieldName = n;
        }
        
        public void fieldType(soot.Type type){
            fieldType = type;
        }

        public void receiver(soot.SootClass sc){
            receiver = sc;
        }*/
            
        /*public void setFieldInst(polyglot.types.FieldInstance fi) {
            fieldInst = fi;
        }*/
        
        public soot.Body getBody(soot.SootMethod sootMethod, String phaseName){
            
            soot.Body body = soot.jimple.Jimple.v().newBody(sootMethod);
            LocalGenerator lg = new LocalGenerator(body);
            
            soot.Local fieldBase = lg.generateLocal(receiver.getType());
            
            // create field type local
            soot.Local fieldLocal = lg.generateLocal(fieldType);
            // assign local to fieldRef
            soot.SootField field = receiver.getField(fieldName, fieldType);

            soot.jimple.FieldRef fieldRef = null;
            if (field.isStatic()) {
                fieldRef = soot.jimple.Jimple.v().newStaticFieldRef(field);
            }
            else {
                body.getUnits().add(soot.jimple.Jimple.v().newIdentityStmt(fieldBase, Jimple.v().newThisRef(receiver.getType())));
                fieldRef = soot.jimple.Jimple.v().newInstanceFieldRef(fieldBase, field);
            }
            soot.jimple.AssignStmt assign = soot.jimple.Jimple.v().newAssignStmt(fieldLocal, fieldRef);
            body.getUnits().add(assign);

            //return local
            soot.jimple.Stmt retStmt = soot.jimple.Jimple.v().newReturnStmt(fieldLocal);
            body.getUnits().add(retStmt);
            
            return body;
         
        }
    }

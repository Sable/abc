/* abc - The AspectBench Compiler
 * Copyright (C) 2004 pavel
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

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
    private boolean isStatic;

    public FieldGetAccessorMethodSource(soot.Type fieldType, String fieldName, soot.SootClass receiver, boolean isStatic) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.receiver = receiver;
        this.isStatic = isStatic;
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
            soot.SootFieldRef field = soot.Scene.v().makeFieldRef(receiver, fieldName, fieldType, isStatic);

            soot.jimple.FieldRef fieldRef = null;
            if (isStatic) {
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

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

/** Abc-specific generalisation of soot.javaToJimple.PrivateMethodAccMethodSource
 * 
 * @author pavel
 */

import java.util.*;
import soot.javaToJimple.Util;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import polyglot.types.MethodInstance;
import soot.RefType;

public class MethodAccessorMethodSource implements soot.MethodSource {

    private MethodInstance methodInst;
    private soot.SootClass receiver;

    /** Constructs a MethodAccessorMethodSource object.
     * 
     * @param mi	The method instance that this is supposed to dispatch to.
     * @param receiver		The RefType of the class it should apply to (the ThisRef is extracted from here), 
     * 					or <code>null</code> if the method is static.
     */
    public MethodAccessorMethodSource(MethodInstance mi, soot.SootClass receiver) {
        methodInst = mi;
        this.receiver = receiver;
    }
    
    public soot.Body getBody(soot.SootMethod sootMethod, String phaseName){
            
        soot.Body body = soot.jimple.Jimple.v().newBody(sootMethod);
        
        soot.Local base = null;
        ArrayList methParams = new ArrayList();
        ArrayList methParamsTypes = new ArrayList();
        // create parameters
        Iterator paramIt = sootMethod.getParameterTypes().iterator();
        int paramCounter = 0;
        while (paramIt.hasNext()) {
            soot.Type sootType = (soot.Type)paramIt.next();
            soot.Local paramLocal = generateLocal(sootType);
            body.getLocals().add(paramLocal);
            soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v().newParameterRef(sootType, paramCounter);
            soot.jimple.Stmt stmt = soot.jimple.Jimple.v().newIdentityStmt(paramLocal, paramRef);
            body.getUnits().add(stmt);
            //System.out.println("next paramType: "+paramLocal.getType());
            methParams.add(paramLocal);
            methParamsTypes.add(paramLocal.getType());
            paramCounter++;
        }
        
        // create return type local
        soot.Type type = Util.getSootType(methodInst.returnType());
        
        soot.Local returnLocal = null;
        if (!(type instanceof soot.VoidType)){
            returnLocal = generateLocal(type);
            body.getLocals().add(returnLocal);
        }

        //System.out.println("meth param types: "+methParamsTypes);
        // assign local to meth
        soot.SootMethodRef meth = soot.Scene.v().makeMethodRef(
                ((soot.RefType)Util.getSootType(methodInst.container())).getSootClass(), 
                methodInst.name(), 
                methParamsTypes, 
                Util.getSootType(methodInst.returnType()),
		methodInst.flags().isStatic());

        soot.jimple.InvokeExpr invoke = null;
        if (methodInst.flags().isStatic()) {
            invoke = soot.jimple.Jimple.v().newStaticInvokeExpr(meth, methParams);
        }
        else if(meth.declaringClass().isInterface()) {
            // TODO: Check this is the correct behaviour - adopted from InterTypeAdjuster
            invoke = soot.jimple.Jimple.v().newInterfaceInvokeExpr(base, meth, methParams);
        }
        else {
            base = generateLocal(receiver.getType());
            body.getLocals().add(base);
            body.getUnits().add(soot.jimple.Jimple.v().newIdentityStmt(base, Jimple.v().newThisRef(receiver.getType())));
            invoke = soot.jimple.Jimple.v().newSpecialInvokeExpr(base, meth, methParams);
        }

        soot.jimple.Stmt stmt = null;
        if (!(type instanceof soot.VoidType)){
            stmt = soot.jimple.Jimple.v().newAssignStmt(returnLocal, invoke);
        }
        else{
            stmt = soot.jimple.Jimple.v().newInvokeStmt(invoke);
        }
        body.getUnits().add(stmt);

        //return local
        soot.jimple.Stmt retStmt = null;
        if (!(type instanceof soot.VoidType)) {
            retStmt = soot.jimple.Jimple.v().newReturnStmt(returnLocal);
        }
        else {
            retStmt = soot.jimple.Jimple.v().newReturnVoidStmt();
        }
        body.getUnits().add(retStmt);
        
        return body;
     
    }
    
    private soot.Local generateLocal(soot.Type type){
        
		String name = "v";
		if (type instanceof soot.IntType) {
			name = nextIntName();
		}
        else if (type instanceof soot.ByteType) {
			name = nextByteName();
		}
        else if (type instanceof soot.ShortType) {
			name = nextShortName();
		}
        else if (type instanceof soot.BooleanType) {
			name = nextBooleanName();
		}
        else if (type instanceof soot.VoidType) {
			name = nextVoidName();
		}
        else if (type instanceof soot.CharType) {
            name = nextIntName();
            type = soot.IntType.v();
        }
		else if (type instanceof soot.DoubleType) {
			name = nextDoubleName();
		}
		else if (type instanceof soot.FloatType) {
			name = nextFloatName();
		}
		else if (type instanceof soot.LongType) {
			name = nextLongName();
		}
        else if (type instanceof soot.RefLikeType) {
            name = nextRefLikeTypeName();
        }
        else {
            //System.out.println("Unhandled Type of local to generate: "+type);
            throw new RuntimeException("Unhandled Type of Local variable to Generate - Not Implemented");
        }
		
		return soot.jimple.Jimple.v().newLocal(name, type);
		
	}

	private int tempInt = -1;
	private int tempVoid = -1;
	private int tempBoolean = -1;
	private int tempLong = -1;
	private int tempDouble = -1;
	private int tempFloat = -1;
    private int tempRefLikeType = -1;
    private int tempByte = -1;
    private int tempShort = -1;
	
    private String nextIntName(){
		tempInt++;
		return "$i"+tempInt;
	}

	private String nextVoidName(){
		tempVoid++;
		return "$v"+tempVoid;
	}

	private String nextByteName(){
		tempByte++;
		return "$b"+tempByte;
	}

	private String nextShortName(){
		tempShort++;
		return "$s"+tempShort;
	}

	private String nextBooleanName(){
		tempBoolean++;
		return "$z"+tempBoolean;
	}

	private String nextDoubleName(){
		tempDouble++;
		return "$d"+tempDouble;
	}
    
	private String nextFloatName(){
		tempFloat++;
		return "$f"+tempFloat;
	}

	private String nextLongName(){
		tempLong++;
		return "$l"+tempLong;
	}

    private String nextRefLikeTypeName(){
        tempRefLikeType++;
        return "$r"+tempRefLikeType;
    }

}

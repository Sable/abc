package abc.soot.util;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.graph.TrapUnitGraph;

// import polyglot.util.InternalCompilerError;

public class Validate {

    public static void validate(SootClass cl) {
	// FIXME: temporary until Soot gets fixed
	Scene.v().releaseActiveHierarchy();
	for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
	    final SootMethod method = (SootMethod) methodIt.next();
	    checkTypes(method);
	    checkInit(method);
	}
    }

    public static void checkTypes(SootMethod method) {
	if(!method.isConcrete()) return;
	Chain units=method.getActiveBody().getUnits();

	Iterator it=units.iterator();
	while(it.hasNext()) {
	    Stmt stmt=(Stmt) (it.next());
	    InvokeExpr iexpr=null;

	    String errorSuffix=" at "+stmt+" in "+method;

	    if(stmt instanceof AssignStmt) {
		AssignStmt astmt=(AssignStmt) stmt;
		Type leftType=Type.toMachineType(astmt.getLeftOp().getType());
		Type rightType=Type.toMachineType(astmt.getRightOp().getType());

		checkCopy(leftType,rightType,errorSuffix);
		if(astmt.getRightOp() instanceof InvokeExpr) 
		    iexpr=(InvokeExpr) (astmt.getRightOp());
	    }

	    if(stmt instanceof InvokeStmt) iexpr=((InvokeStmt) stmt).getInvokeExpr();

	    if(iexpr!=null) {
		SootMethod called=iexpr.getMethod();

		if(iexpr instanceof InstanceInvokeExpr) {
		    InstanceInvokeExpr iiexpr=(InstanceInvokeExpr) iexpr;
		    checkCopy(called.getDeclaringClass().getType(),
			      iiexpr.getBase().getType(),
			      " in receiver of call"+errorSuffix);
		}

		if(called.getParameterCount() != iexpr.getArgCount())
		    System.err.println("Warning: Argument count doesn't match up with signature in call"+errorSuffix);
		else 
		    for(int i=0;i<iexpr.getArgCount();i++)
			checkCopy(Type.toMachineType(called.getParameterType(i)),
				  Type.toMachineType(iexpr.getArg(i).getType()),
				  " in argument "+i+" of call"+errorSuffix);
	    }
	}
    }

    public static void checkCopy(Type leftType,Type rightType,String errorSuffix) {
	if(leftType instanceof PrimType || rightType instanceof PrimType) {
	    if(leftType instanceof IntType && rightType instanceof IntType) return;
	    if(leftType instanceof LongType && rightType instanceof LongType) return;
	    if(leftType instanceof FloatType && rightType instanceof FloatType) return;
	    if(leftType instanceof DoubleType && rightType instanceof DoubleType) return;
	    System.err.println("Warning: Bad use of primitive type"+errorSuffix);
	}

	if(rightType instanceof NullType) return;
	if(leftType instanceof RefType &&
	   ((RefType) leftType).getClassName().equals("java.lang.Object")) return;
	
	if(leftType instanceof ArrayType || rightType instanceof ArrayType) {
	    if(leftType instanceof ArrayType && rightType instanceof ArrayType) return;

	    System.err.println("Warning: Bad use of array type"+errorSuffix);
	    return;
	}

	if(leftType instanceof RefType && rightType instanceof RefType) {
	    SootClass leftClass=((RefType) leftType).getSootClass();
	    SootClass rightClass=((RefType) rightType).getSootClass();
	    
	    if(leftClass.isInterface()) {
		if(rightClass.isInterface()) {
		    if(!(leftClass.getName().equals(rightClass.getName()) || 
			 Scene.v().getActiveHierarchy().isInterfaceSubinterfaceOf(rightClass,leftClass)))
			System.err.println("Warning: Bad use of interface type"+errorSuffix);
		} else {
		    // No quick way to check this for now.
		}
	    } else {
		if(rightClass.isInterface()) {
		    System.err.println("Warning: trying to use interface type where non-Object class expected"
				       +errorSuffix);
		} else {
		    if(!Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(rightClass,leftClass))
			System.err.println("Warning: Bad use of class type"+errorSuffix);
		}
	    }
	    return;
	}
	System.err.println("Warning: Bad types"+errorSuffix);
    }

    public static void checkInit(SootMethod method) {
	if(!method.isConcrete()) return;

	InitAnalysis analysis=new InitAnalysis(new TrapUnitGraph(method.getActiveBody()));
	Chain units=method.getActiveBody().getUnits();

	Iterator it=units.iterator();
	while(it.hasNext()) {
	    Stmt s=(Stmt) (it.next());
	    FlowSet init=(FlowSet) analysis.getFlowBefore(s);
	    List uses=s.getUseBoxes();
	    Iterator usesIt=uses.iterator();
	    while(usesIt.hasNext()) {
		Value v=((ValueBox) (usesIt.next())).getValue();
		if(v instanceof Local) {
		    Local l=(Local) v;
		    if(!init.contains(l))
			System.err.println("Warning: Local variable "+l
					   +" not definitely defined at "+s
					   +" in "+method);
		}
	    }
	}
    }

}

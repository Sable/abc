package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Check the type of a context value
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class CheckType extends Residue {
    private ContextValue value;
    private Type type;

    private CheckType(ContextValue value,Type type) {
	this.value=value;
	this.type=type;
    }

    // It's important that we throw away statically invalid matches
    // here rather than at code generation time, because if we wait until
    // then the code for a corresponding Bind will probably be generated
    // as well, and will be type incorrect; although it will be dead code,
    // the Soot code generator still won't be happy.

    public static Residue construct(ContextValue value,Type type) {
	if(type.equals(Scene.v().getSootClass("java.lang.Object").getType())) 
	    return AlwaysMatch.v;


	Type from=value.getSootType();
	Type to=type;
	if(from instanceof PrimType || to instanceof PrimType) {
	    if(from.equals(to)) return AlwaysMatch.v;

	    // FIXME: check that the Java widening primitive conversions are
	    // the right thing to do in this context
	    // attempts to create a test case crash ajc, which makes things hard

	    if(from instanceof ByteType) from=ShortType.v();
	    if(from.equals(to)) return AlwaysMatch.v;

	    if(from instanceof ShortType || from instanceof CharType) 
		from=IntType.v();
	    if(from.equals(to)) return AlwaysMatch.v;

	    if(from instanceof IntType) from=LongType.v();
	    if(from.equals(to)) return AlwaysMatch.v;

	    if(from instanceof LongType) from=FloatType.v();
	    if(from.equals(to)) return AlwaysMatch.v;

	    if(from instanceof FloatType) from=DoubleType.v();
	    if(from.equals(to)) return AlwaysMatch.v;

	    return NeverMatch.v;
	} else {
	    FastHierarchy hier=Scene.v().getOrMakeFastHierarchy();

	    if(from instanceof NullType) return NeverMatch.v;

	    if(hier.canStoreType(from,to)) 
	    	return AlwaysMatch.v;
	    // For strict ajc compliance, we *must* eliminate this much, and
	    // anything further we decide we can eliminate (e.g. using a global analysis)
	    // must be replaced by an "is not null" check
	    // This is because if ajc treats null differently if it 
	    // eliminates the static type check than if it doesn't.

	}

	Residue res=new CheckType(value,type);

	if(!abc.main.Debug.v().ajcCompliance)
	    // When not in ajc compliance mode, we always consider that null
	    // is a valid instance of any (reference) type
	    res=OrResidue.construct(new IsNull(value),res);

	return new CheckType(value,type);
    }

    public String toString() {
	return "checktype("+value+","+type+")";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	Value v=value.getSootValue();
	Local io=localgen.generateLocal(BooleanType.v(),"checkType");
	Stmt instancetest
	    =Jimple.v().newAssignStmt(io,Jimple.v().newInstanceOfExpr(v,type));
	Expr test;
	if(sense) test=Jimple.v().newEqExpr(io,IntConstant.v(0));
	else test=Jimple.v().newNeExpr(io,IntConstant.v(0));
	Stmt abort=Jimple.v().newIfStmt(test,fail);
	units.insertAfter(instancetest,begin);
	units.insertAfter(abort,instancetest);
	return abort;
    }

}

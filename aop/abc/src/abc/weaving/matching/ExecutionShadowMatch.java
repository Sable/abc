package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import abc.soot.util.Restructure;

/** The results of matching at an execution shadow.
 *  abc does a front-end transformation that means that static initialization
 *  and advice execution join point shadows are also treated as execution shadows
 *  @author Ganesh Sittampalam
 */
public class ExecutionShadowMatch extends BodyShadowMatch {

    // Because this is a potential target for getEnclosing(),
    // we want to ensure that there is a unique instance per method

    private ExecutionShadowMatch(SootMethod container) {
	super(container);
    }

    private static Hashtable/*<SootMethod,ExecutionShadowMatch>*/ esms=new Hashtable();
    public static void reset() {
	esms=new Hashtable();
    }

    static ExecutionShadowMatch construct(SootMethod container) {
	if(esms.containsKey(container)) return (ExecutionShadowMatch) esms.get(container);
	ExecutionShadowMatch esm=new ExecutionShadowMatch(container);
	esms.put(container,esm);
	return esm;
    }

    public static ExecutionShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Execution");

	return construct(pos.getContainer());
    }

    public List/*<SootClass>*/ getExceptions() {
	return container.getExceptions();
    }

    public SJPInfo makeSJPInfo() {
	String jpKind;
	String sigClass;
	String sigMethod;
	String sig;
	if(isStaticInitializer()) {
	    jpKind="staticinitialization";
	    sigClass="InitializerSignature";
	    sigMethod="makeInitializerSig"; 
	    sig=SJPInfo.makeStaticInitializerSigData(container);
	} else if(isConstructor()) {
	    jpKind="constructor-execution";
	    sigClass="ConstructorSignature";
	    sigMethod="makeConstructorSig";
	    sig=SJPInfo.makeConstructorSigData(container);
	} else if(isAdviceBody()) {
	    jpKind="advice-execution";
	    sigClass="AdviceSignature";
	    sigMethod="makeAdviceSig";
	    sig=SJPInfo.makeAdviceSigData(container);
	} else {
	    jpKind="method-execution";
	    sigClass="MethodSignature";
	    sigMethod="makeMethodSig";
	    sig=SJPInfo.makeMethodSigData(container);
	}

	return new SJPInfo
	    (jpKind,sigClass,sigMethod,sig,getHost());
    }

    private boolean isStaticInitializer() {
	return container.getName().equals(SootMethod.staticInitializerName);
    }

    private boolean isConstructor() {
	return container.getName().equals(SootMethod.constructorName);
    }

    private boolean isAdviceBody() {
	return MethodCategory.adviceBody(container);
    }

    public Host getHost() {
	// FIXME:  this is close to what we want,  but in the case of
	//            a constructor execution we really want the position
	//            of the first statement after the super()
	// FIXME:  this works for static initialization when there are
	//         real static initializers in there.  Otherwise we
	//         should report the line number of the beginning of
	//         the class being initialized.
        // TODO:  rethink the structure of this code .... where should we
	//          find the position??  should have some utililty 
	//          methods?
	// Want to return the first "real" statement of the body that
	//   is not an identity statement or a nop

	Stmt firstRealStmt = Restructure.findFirstRealStmt
	    (container,container.getActiveBody().getUnits());

	return firstRealStmt;
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {
	ExecutionAdviceApplication aa=new ExecutionAdviceApplication(ad,residue);
	mal.addBodyAdvice(aa);
	return aa;
    }

    public ContextValue getReturningContextValue() {

	if(container.getName().equals(SootMethod.staticInitializerName) ||
	   container.getName().equals(SootMethod.constructorName))
	    return super.getReturningContextValue();  // null value

	Stmt nop=Restructure.restructureReturn(container);
	Chain units=container.getActiveBody().getUnits();
	Stmt ret=(Stmt) units.getSuccOf(nop);

	if(ret instanceof ReturnVoidStmt)
	    return super.getReturningContextValue();  // null value
	else if(ret instanceof ReturnStmt)
	    return new JimpleValue(((ReturnStmt) ret).getOp());
	else throw new RuntimeException
		 ("restructureReturn didn't restructure returns correctly");
	   
    }

    public String joinpointName() {
	if(isStaticInitializer()) return "staticinitialization";
	if(isConstructor()) return "constructor execution";
	if(isAdviceBody()) return "advice execution";
	return "method execution";
    }

}

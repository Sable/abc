package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import java.util.*;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import abc.soot.util.Restructure;

/** The results of matching at an execution shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ExecutionShadowMatch extends BodyShadowMatch {

    ExecutionShadowMatch(SootMethod container) {
	super(container);
    }

    public static ExecutionShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return new ExecutionShadowMatch(pos.getContainer());
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	String jpKind;
	String sigClass;
	String sigMethod;
	String sig="";
	if(container.getName().equals(SootMethod.staticInitializerName)) {
	    jpKind="staticinitialization";
	    sigClass="InitializerSignature";
	    sigMethod="makeInitializerSig"; 
	} else if(container.getName().equals(SootMethod.constructorName)) {
	    jpKind="constructor-execution";
	    sigClass="ConstructorSignature";
	    sigMethod="makeConstructorSig";
	} else { // add advice-execution case
	    jpKind="method-execution";
	    sigClass="MethodSignature";
	    sigMethod="makeMethodSig";
	}

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
	Stmt firstRealStmt = null;
	// Want to return the first "real" statement of the body that
	//   is not an identity statement or a nop
	Iterator it = container.getActiveBody().getUnits().iterator();
	while (it.hasNext())
	   { firstRealStmt = (Stmt) it.next();
	     if (! (firstRealStmt instanceof IdentityStmt) &&
		 ! (firstRealStmt instanceof NopStmt) )
	       break;
	   }
	return new AdviceApplication.SJPInfo
	    (jpKind,sigClass,sigMethod,sig,firstRealStmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {
	ExecutionAdviceApplication aa=new ExecutionAdviceApplication(ad,residue);
	mal.addBodyAdvice(aa);
	return aa;
    }

    public ContextValue getReturningContextValue() {

	if(container.getName().equals(SootMethod.staticInitializerName) ||
	   container.getName().equals(SootMethod.constructorName) ||
	   MethodCategory.adviceBody(container)) 
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
}

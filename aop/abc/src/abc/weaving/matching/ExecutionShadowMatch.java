package abc.weaving.matching;

import soot.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an execution shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ExecutionShadowMatch extends ShadowMatch {
    private SootMethod container;

    public ShadowMatch getEnclosing() {
	return this;
    }

    ExecutionShadowMatch(SootMethod container) {
	this.container=container;
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
	return new AdviceApplication.SJPInfo
	    (jpKind,sigClass,sigMethod,sig,container.getActiveBody());
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {
	ExecutionAdviceApplication aa=new ExecutionAdviceApplication(ad,residue);
	mal.addBodyAdvice(aa);
	return aa;
    }
}

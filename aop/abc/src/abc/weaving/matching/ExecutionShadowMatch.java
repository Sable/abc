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

    private ExecutionShadowMatch(SootMethod container) {
	this.container=container;
    }

    public static ExecutionShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return new ExecutionShadowMatch(((WholeMethodPosition) pos).container);
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	String jpKind;
	String sigMethod;
	String sig=null;
	if(container.getName().equals(SootMethod.staticInitializerName)) {
	    jpKind="constructor-execution";
	    sigMethod="makeConstructorSig";
	} else if(container.getName().equals(SootMethod.constructorName)) {
	    System.out.println("LJH container.getName() " +
		               container.getName() +
			       " SootMethod.constructorName " +
			       SootMethod.constructorName);
	    jpKind="staticinitialization";
	    sigMethod=null;
	} else { // add advice-execution case
	    jpKind="method-execution";
	    sigMethod="makeMethodSig";
	}
	AdviceApplication.SJPInfo sjpInfo
	    =new AdviceApplication.SJPInfo(jpKind,sigMethod,sig,-1,-1);
	mal.addBodyAdvice(new ExecutionAdviceApplication(ad,residue,sjpInfo));
    }
}

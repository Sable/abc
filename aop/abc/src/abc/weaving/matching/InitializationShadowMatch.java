package abc.weaving.matching;

import java.util.List;

import soot.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an initialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class InitializationShadowMatch extends BodyShadowMatch {

    private InitializationShadowMatch(SootMethod container) {
	super(container);
    }

    public List/*<SootClass>*/ getExceptions() {
	return container.getExceptions();
    }

    public static InitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Initialization");

	SootMethod container=pos.getContainer();
	if(!container.getName().equals(SootMethod.constructorName)) return null;
	return new InitializationShadowMatch(container);
    }

    public Host getHost() {
	// FIXME: should point to first real statement or something
	return container;
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("initialization","ConstructorSignature","makeConstructorSig",
	     SJPInfo.makeConstructorSigData(container),getHost());
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	InitializationAdviceApplication aa
	    =new InitializationAdviceApplication(ad,residue);
	mal.addInitializationAdvice(aa);
	return aa;
    }

    public boolean supportsAround() {
	return false;
    }

    public String joinpointName() {
	return "initialization";
    }

}

package abc.weaving.matching;

import java.util.List;

import soot.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at an class initialization shadow
 *  @author Ganesh Sittampalam
 */
public class ClassInitializationShadowMatch extends BodyShadowMatch {

    public static ShadowType shadowtype = new ShadowType() {
	    public ShadowMatch matchesAt(MethodPosition pos) {
		return ClassInitializationShadowMatch.matchesAt(pos);
	    }
	};

    public static void register() {
	ShadowType.register(shadowtype);
    }

    private ClassInitializationShadowMatch(SootMethod container) {
	super(container);
    }

    public List/*<SootClass>*/ getExceptions() {
	return container.getExceptions();
    }

    public static ClassInitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Initialization");

	SootMethod container=pos.getContainer();
	if(!container.getName().equals(SootMethod.constructorName)) return null;
	return new ClassInitializationShadowMatch(container);
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("initialization","ConstructorSignature","makeConstructorSig",
	     SJPInfo.makeConstructorSigData(container),getHost());
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	ClassInitializationAdviceApplication aa
	    =new ClassInitializationAdviceApplication(ad,residue);
	mal.addInitializationAdvice(aa);
	return aa;
    }

    // ajc doesn't support this, but we do
    /*
    public boolean supportsAround() {
	return false;
    }
    */

    public String joinpointName() {
	return "class initialization";
    }

}

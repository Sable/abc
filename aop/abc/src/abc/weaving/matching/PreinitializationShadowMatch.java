package abc.weaving.matching;

import soot.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** The results of matching at an preinitialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class PreinitializationShadowMatch extends ShadowMatch {
    private SootMethod container;

    private PreinitializationShadowMatch(SootMethod container) {
	this.container=container;
    }

    public ShadowMatch getEnclosing() {
	return this;
    }

    public static PreinitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	SootMethod container=pos.getContainer();
	if(!container.getName().equals(SootMethod.constructorName)) return null;
	return new PreinitializationShadowMatch(container);
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	return new AdviceApplication.SJPInfo
	    ("preinitialization","ConstructorSignature","makeConstructorSig","",container);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	PreinitializationAdviceApplication aa
	    =new PreinitializationAdviceApplication(ad,residue);
	mal.addPreinitializationAdvice(aa);
	return aa;
    }

    public ContextValue getThisContextValue(SootMethod method) {
        return null;
    }
}

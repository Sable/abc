package abc.weaving.matching;

import soot.*;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** The results of matching at a particular shadow type
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public abstract class ShadowMatch {
    /** return the enclosing ShadowMatch */
    public abstract ShadowMatch getEnclosing();

    /** construct the sjpInfo structure */
    public abstract AdviceApplication.SJPInfo makeSJPInfo();

    private AdviceApplication.SJPInfo sjpInfo=null;

    /** retrieve the sjpInfo structure */
    public final AdviceApplication.SJPInfo getSJPInfo() {
	if(sjpInfo==null) sjpInfo=makeSJPInfo();
	return sjpInfo;
    }
    

    /** Add a new advice application to the appropriate bit of a 
	method advice list */
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	AdviceApplication aa=doAddAdviceApplication(mal,ad,residue);
	if(ad.hasJoinPoint() || ad.hasJoinPointStaticPart()) {
	    aa.sjpInfo=getSJPInfo();
	}
	if(ad.hasEnclosingJoinPoint()) {
	    aa.sjpEnclosing=getEnclosing().getSJPInfo();
	    getEnclosing().addDummyAdviceApplication(mal);
	}
    }

    private void addDummyAdviceApplication(MethodAdviceList mal) {
	AdviceApplication aa=doAddAdviceApplication(mal,null,AlwaysMatch.v);
	aa.sjpInfo=getSJPInfo();
    }

    protected abstract AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue);

    // FIXME: move this to subclasses(?)
    public ContextValue getThisContextValue(SootMethod method) {
        return method.isStatic() ? null : new This();
    }
}

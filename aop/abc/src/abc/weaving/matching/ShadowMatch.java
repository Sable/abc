package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;
import abc.weaving.weaver.LocalGeneratorEx;
import abc.soot.util.Restructure;

/** The results of matching at a particular shadow type
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public abstract class ShadowMatch {
    protected SootMethod container;

    protected ShadowMatch(SootMethod container) {
	this.container=container;
    }

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
	    ShadowMatch enclosing=getEnclosing();
	    aa.sjpEnclosing=enclosing.getSJPInfo();
	    enclosing.addDummyAdviceApplication(mal);
	}
    }

    private void addDummyAdviceApplication(MethodAdviceList mal) {
	AdviceApplication aa=doAddAdviceApplication(mal,null,AlwaysMatch.v);
	aa.sjpInfo=getSJPInfo();
    }

    protected abstract AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue);

    public ContextValue getThisContextValue() {
        if(container.isStatic()) return null;
	return new JimpleValue(Restructure.makeThisCopy(container));
    }

    // no sensible default
    public abstract ContextValue getTargetContextValue();

    // no sensible default
    public ContextValue getReturningContextValue() {
	// remove when subclasses implement it
	throw new RuntimeException("No returning context value implemented "+this);
    }

    public boolean supportsBefore() {
	return true;
    }
    public boolean supportsAfter() {
	return true;
    }
    public boolean supportsAround() {
	return supportsBefore() && supportsAfter();
    }

}

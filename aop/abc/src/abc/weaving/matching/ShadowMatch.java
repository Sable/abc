package abc.weaving.matching;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.*;
import abc.soot.util.LocalGeneratorEx;
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
    protected abstract AdviceApplication.SJPInfo makeSJPInfo();

    private AdviceApplication.SJPInfo sjpInfo=null;

    /** retrieve the sjpInfo structure */
    public final AdviceApplication.SJPInfo getSJPInfo() {
	if(sjpInfo==null) sjpInfo=makeSJPInfo();
	return sjpInfo;
    }
    

    /** Add a new advice application to the appropriate bit of a 
	method advice list */
    public void addAdviceApplication(MethodAdviceList mal,
				     AbstractAdviceDecl ad,
				     Residue residue) {
	AdviceApplication aa=doAddAdviceApplication(mal,ad,residue);
	aa.setShadowMatch(this);
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
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue);

    public ContextValue getThisContextValue() {
        if(container.isStatic()) return null;
	return new JimpleValue(Restructure.getThisCopy(container));
    }

    // no sensible default - unless null?
    public abstract ContextValue getTargetContextValue();

    public List/*<ContextValue>*/ getArgsContextValues() {
	// replace by empty list later?
	throw new RuntimeException("args not yet implemented for "+this);
    }

    public ContextValue getReturningContextValue() {
	return new JimpleValue(NullConstant.v());
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

    public abc.weaving.weaver.ShadowPoints sp=null;
    public void setShadowPoints(abc.weaving.weaver.ShadowPoints sp) {
	this.sp=sp;
    }

}

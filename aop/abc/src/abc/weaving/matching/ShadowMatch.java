package abc.weaving.matching;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.*;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.*;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;

import abc.weaving.aspectinfo.MethodCategory;

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

    public SootMethod getContainer() {
	return container;
    }

    /** Get the host that this ShadowMatch corresponds to,
     *  for positional information 
     */
    public abstract Host getHost();

    /** construct the sjpInfo structure */
    protected abstract SJPInfo makeSJPInfo();

    private SJPInfo sjpInfo=null;

    /** retrieve the sjpInfo structure */
    public final SJPInfo getSJPInfo() {
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
        if(container.isStatic()   && 
        	!MethodCategory.hasThisAsFirstParameter(container) ) return null;
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

    /** The list of exceptions that this shadow is declared to throw */
    public List/*<SootClass>*/ getExceptions() {
	/* default to empty */
	return new ArrayList();
    }

    public abc.weaving.weaver.ShadowPoints sp=null;
    public void setShadowPoints(abc.weaving.weaver.ShadowPoints sp) {
	this.sp=sp;
    }

}

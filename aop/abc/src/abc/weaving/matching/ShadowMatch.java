package abc.weaving.matching;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.*;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
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

    /** record the sjpInfo structure if necessary */
    public final void recordSJPInfo() {
	if(sjpInfo!=null) return;
	sjpInfo=makeSJPInfo();
	GlobalAspectInfo.v().addSJPInfo(container,sjpInfo);
    }

    /** retrieve the sjpInfo structure, creating it if necessary */
    public final SJPInfo getSJPInfo() {
	recordSJPInfo();
	return sjpInfo;
    }
    
    private boolean recorded=false;

    /** Add a new advice application to the appropriate bit of a 
	method advice list */
    public void addAdviceApplication(MethodAdviceList mal,
				     AbstractAdviceDecl ad,
				     Residue residue) {
	AdviceApplication aa=doAddAdviceApplication(mal,ad,residue);
	ad.incrApplCount();
	aa.setShadowMatch(this);
	if(!recorded) {
            GlobalAspectInfo.v().addShadowMatch(container,this);
            recorded=true;
        }
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
	sp.setShadowMatch(this);
    }

}

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

/** A specific join point shadow
 *  @author Ganesh Sittampalam
 */
public abstract class ShadowMatch {
    protected SootMethod container;

    protected ShadowMatch(SootMethod container) {
	this.container=container;
    }

    /** return the enclosing ShadowMatch */
    public abstract ShadowMatch getEnclosing();

    /** return the method that this ShadowMatch occurs within */
    public SootMethod getContainer() {
	return container;
    }

    /** Get the host that this ShadowMatch corresponds to,
     *  for positional information 
     */
    public abstract Host getHost();

    /** Construct the sjpInfo structure */
    protected abstract SJPInfo makeSJPInfo();

    private SJPInfo sjpInfo=null;

    /** Construct the sjpInfo structure and register it with the 
     *  global list in GlobalAspectInfo, unless this has already been done */
    public final void recordSJPInfo() {
	if(sjpInfo!=null) return;
	sjpInfo=makeSJPInfo();
	GlobalAspectInfo.v().addSJPInfo(container,sjpInfo);
    }

    /** Retrieve the sjpInfo structure, creating it if necessary */
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

    /** Return a ContextValue that represents the runtime value 
     *  that is bound by a this() pointcut 
     */
    public ContextValue getThisContextValue() {
        if(container.isStatic()   && 
        	!MethodCategory.hasThisAsFirstParameter(container) ) return null;
        	
        	//System.out.println(" " + container + " : " + 
        	//				container.getActiveBody().getLocalCount());
        	//System.out.println(container.getActiveBody().getUnits().getFirst());
        	//System.out.println(container.isStatic());
		return new JimpleValue(Restructure.getThisLocal(container));
    }

    /** Return a ContextValue that represents the runtime value 
     *  that is bound by a target() pointcut 
     */
    // no sensible default - unless null?
    public abstract ContextValue getTargetContextValue();

    /** Return a list of ContextValue that represent the runtime values
     *  that could be bound by an args() pointcut
     */
    public List/*<ContextValue>*/ getArgsContextValues() {
	// replace by empty list later?
	throw new RuntimeException("args not yet implemented for "+this);
    }

    /** Return a ContextValue that represents the runtime value 
     *  that is bound by after returning() advice
     */
    public ContextValue getReturningContextValue() {
	return new JimpleValue(NullConstant.v());
    }

    /** Does this shadow support before advice? */
    public boolean supportsBefore() {
	return true;
    }
    /** Does this shadow support after advice? */
    public boolean supportsAfter() {
	return true;
    }
    /** Does this shadow support around advice? */
    public boolean supportsAround() {
	return supportsBefore() && supportsAfter();
    }

    /** The list of exceptions that this shadow is declared to throw */
    public List/*<SootClass>*/ getExceptions() {
	/* default to empty */
	return new ArrayList();
    }

    /** The ShadowPoints of a specific shadow match are the nop statements
     *  that are inserted to make weaving easier. 
     *  They are set by the ShadowPointsSetter
     */
    public abc.weaving.weaver.ShadowPoints sp=null;

    /** This method is called by the ShadowPointsSetter to set the
     *  ShadowPoints for this shadow
     */
    public void setShadowPoints(abc.weaving.weaver.ShadowPoints sp) {
	this.sp=sp;
	sp.setShadowMatch(this);
    }

    public abstract String joinpointName();
}

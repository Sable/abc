package abc.weaving.matching;

import java.util.List;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import soot.util.Chain;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.IntertypeAdjuster;
import abc.weaving.weaver.ShadowPoints;

/** The results of matching at an interface initialization shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class InterfaceInitializationShadowMatch extends BodyShadowMatch {

    public static ShadowType shadowtype = new ShadowType() {
	    public ShadowMatch matchesAt(MethodPosition pos) {
		return InterfaceInitializationShadowMatch.matchesAt(pos);
	    }
	};

    public static void register() {
	ShadowType.register(shadowtype);
    }

    protected SootClass intrface;

    public SootClass getInterface() {
	return intrface;
    }

    private InterfaceInitializationShadowMatch(SootMethod container,SootClass intrface,ShadowPoints sp) {
	super(container);
	this.intrface=intrface;
	setShadowPoints(sp);
    }

    public List/*<SootClass>*/ getExceptions() {
	return container.getExceptions();
    }

    public static InterfaceInitializationShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Interface Initialization");

	SootMethod container=pos.getContainer();
	if(!container.getName().equals(SootMethod.constructorName)) return null;

	Stmt startNop=((StmtMethodPosition) pos).getStmt();
	if(!startNop.hasTag(IntertypeAdjuster.InterfaceInitNopTag.name)) return null;

	IntertypeAdjuster.InterfaceInitNopTag tag
	    =(IntertypeAdjuster.InterfaceInitNopTag) 
	    startNop.getTag(IntertypeAdjuster.InterfaceInitNopTag.name);

	if(!tag.isStart) return null;

	Chain units=container.getActiveBody().getUnits();
	Stmt endNop=(Stmt) units.getSuccOf(startNop);

	while(endNop!=null) {
	    if(endNop.hasTag(IntertypeAdjuster.InterfaceInitNopTag.name)) break;
	    endNop=(Stmt) units.getSuccOf(endNop);
	}
	if(endNop==null) 
	    throw new InternalCompilerError
		("Failed to find ending nop for interface "
		 +tag.intrface+" initialization in "+container);

	return new InterfaceInitializationShadowMatch(container,tag.intrface,new ShadowPoints(container,startNop,endNop));
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("initialization","ConstructorSignature","makeConstructorSig",
	     SJPInfo.makeInitializationSigData(intrface),getHost());
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	InterfaceInitializationAdviceApplication aa
	    =new InterfaceInitializationAdviceApplication(ad,residue,intrface);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public String joinpointName() {
	return "interface initialization";
    }

    public void setShadowPoints(Chain units) {
    }
}

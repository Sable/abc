package abc.weaving.aspectinfo;

import java.util.Hashtable;
import java.util.Set;

import polyglot.types.SemanticException;
import soot.SootClass;
import soot.SootMethod;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;

import polyglot.util.Position;

/** Any Cflow-like pointcut. This stores a child pointcut,
 *  and the setup advice that goes with it
 * @author Damien Sereni
 */
public abstract class CflowPointcut extends Pointcut {

	public CflowPointcut(Position pos) {
		super(pos);
	}

	// The child pointcut
	
	private Pointcut pc;
	
    public Pointcut getPointcut() { return pc; }
    protected void setPointcut(Pointcut pc) { this.pc = pc; }
	
	// Storing the setup advice
	
	private CflowSetup setupadvice;
	private Hashtable/*<Var,PointcutVarEntry>*/ renaming;
	
	CflowSetup getCfs() { return setupadvice; }
	protected void setCfs(CflowSetup cfs) { this.setupadvice = cfs; } 
	
	protected Hashtable/*<Var,PointcutVarEntry>*/ getRenaming() { return renaming; }
	protected void setRenaming(Hashtable/*<Var,PointcutVarEntry>*/ renaming ) {
		this.renaming = renaming;
	}
	
	private Hashtable/*<String,AbcType>*/ typeMap;
	Hashtable/*<String, AbcType>*/ getTypeMap() {
		return typeMap;
	}
	protected void setTypeMap(Hashtable/*<String,AbcType>*/ typeMap) {
		this.typeMap = typeMap;
	}
	
	protected void reRegisterSetupAdvice(
			CflowSetup cfs, Hashtable/*<Var,PointcutVarEntry>*/ ren) {
		
		if (abc.main.Debug.v().debugCflowSharing)
			System.out.println("@@@@ "+pc+"\n@@@@ changed CFS");
		
		setCfs(cfs);
		setRenaming(ren);
		getCfs().addUse(this);
	}
	
}

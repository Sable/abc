package abc.ja.cjp.weaving;

import polyglot.util.Position;
import soot.SootMethod;
import abc.weaving.aspectinfo.ShadowPointcut;
import abc.weaving.matching.MethodCallShadowMatch;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;

public class CJPCall extends ShadowPointcut {

	private final String fullyQualifiedName;

	public CJPCall(String fqnOfCJP, Position pos) {
		super(pos);
		this.fullyQualifiedName = fqnOfCJP;
	}

	@Override
	protected Residue matchesAt(ShadowMatch sm) {
		if(!(sm instanceof MethodCallShadowMatch)) return NeverMatch.v();
		MethodCallShadowMatch mcsm = (MethodCallShadowMatch) sm;
		SootMethod target = mcsm.getMethodRef().resolve();
		if(!target.hasTag("abc.ja.cjp.weaving.ExtractedTag")) return NeverMatch.v();
		ExtractedTag tag = (ExtractedTag)target.getTag("abc.ja.cjp.weaving.ExtractedTag");
		if(!fullyQualifiedName.equals(tag.getCjpTypeName())) return NeverMatch.v(); 
		return AlwaysMatch.v();
	}

	@Override
	public String toString() {
		return "cjpCall("+fullyQualifiedName+")";
	}

}

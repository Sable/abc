package abc.ja.om.aspectinfo;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.ClassnamePatternExpr_c;
import abc.ja.om.jrag.Pattern;
import abc.om.modulestruct.ModuleNodeClass;

public class JAModuleNodeClass extends ModuleNodeClass {

	protected Pattern pat;
	
	public JAModuleNodeClass(String parentName, Pattern pat, Position pos) {
		this.cpe = null;
		this.pat = pat;
		name = parentName + "." + pat.toString();
		this.pos = pos;
	}
	
	@Override
	public ClassnamePatternExpr getCPE() {
		throw new InternalCompilerError("Attempt to get Polyglot CPE from JAModuleNodeClass");
	}

	public Pattern getCPEPattern() {
		return pat;
	}
}

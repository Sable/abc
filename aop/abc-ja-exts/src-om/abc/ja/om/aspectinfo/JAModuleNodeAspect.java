package abc.ja.om.aspectinfo;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.visit.PCNode;
import abc.ja.om.jrag.Pattern;
import abc.main.CompilerFailedException;
import abc.om.visit.ModuleNodeAspect;

public class JAModuleNodeAspect extends ModuleNodeAspect {
	
	protected Pattern pat;
	protected boolean found = false; //true if an aspect matching the pattern has been found
	
	public JAModuleNodeAspect(String name, Pattern pat, Position pos) {
		this.aspectNode = null;
        this.cpe = null;
        this.name = name;
        this.pos = pos;
		this.pat = pat;
	}
	
	@Override
	public ClassnamePatternExpr getCPE() {
		throw new InternalCompilerError("Attempt to get Polyglot CPE from JAModuleNodeClass");
	}

	public Pattern getCPEPattern() {
		return pat;
	}
	public PCNode getAspectNode() {
        throw new InternalCompilerError("Attempt to use Polyglot version JAModuleNodeAspect.getAspectNode()");
    }

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}
	
}

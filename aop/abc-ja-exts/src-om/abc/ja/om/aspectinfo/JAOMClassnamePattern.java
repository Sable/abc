package abc.ja.om.aspectinfo;

import polyglot.util.InternalCompilerError;
import soot.SootClass;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.ja.om.jrag.Pattern;
import abc.weaving.aspectinfo.ClassnamePattern;

public class JAOMClassnamePattern implements ClassnamePattern {
	Pattern pattern;
	
	public JAOMClassnamePattern(Pattern pat) {
		this.pattern = pat;
	}
	
	public boolean equivalent(ClassnamePattern p) {
		return false;
	}

	public ClassnamePatternExpr getPattern() {
        throw new InternalCompilerError("Can not get polyglot frontend pattern from JastAdd");
    }

	public boolean matchesClass(SootClass cl) {
    	if(abc.main.Debug.v().patternMatches) {
    		System.err.println("Matching classname pattern " + pattern + " against "
    				+ cl + ": " + pattern.matchesType(cl.getType()));
    	}
        return pattern.matchesType(cl);	}
	
	public String toString() {
		return pattern.toString();
	}
}

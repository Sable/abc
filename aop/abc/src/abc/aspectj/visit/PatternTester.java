
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

public class PatternTester extends NodeVisitor {
    protected abc.aspectj.ExtensionInfo ext_info;

    public PatternTester(abc.aspectj.ExtensionInfo ext_info) {
	this.ext_info = ext_info;
    }

    public Node override(Node n) {
	if (n instanceof ContainsNamePattern) {
	    NamePattern pat = ((ContainsNamePattern)n).getNamePattern();
	    Position p = pat.position();
	    System.out.println("The name pattern "+pat+" on "+p+" matches these names:");
	    Set matches = ext_info.pattern_matcher.getMatches(pat);
	    Iterator mi = matches.iterator();
	    while (mi.hasNext()) {
		PCNode m = (PCNode)mi.next();
		System.out.println(m);
	    }
	    return n;
	}
	return null;
    }

}

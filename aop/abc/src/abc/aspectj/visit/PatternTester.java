
package arc.aspectj.visit;

import arc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

public class PatternTester extends NodeVisitor {
    protected arc.aspectj.ExtensionInfo ext_info;

    public PatternTester(arc.aspectj.ExtensionInfo ext_info) {
	this.ext_info = ext_info;
    }

    public Node override(Node n) {
	if (n instanceof NamePattern) {
	    Position p = n.position();
	    System.out.println("The name pattern on "+p.file()+":"+p.line()+" matches these names:");
	    Set matches = ext_info.pattern_matcher.getMatches((NamePattern)n);
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

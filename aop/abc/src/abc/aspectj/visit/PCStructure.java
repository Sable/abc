
package arc.aspectj.visit;

import java.util.*;
import java.util.regex.*;

import arc.aspectj.ast.*;

public class PCStructure {
    private PCNode root;

    public PCStructure() {
	root = new PCNode(null, null);
    }

    public PCNode insertFullName(String full_name) {
	return root.insertFullName(full_name);
    }

    public void declareParent(String child, String parent) {
	PCNode cn = root.insertFullName(child);
	PCNode pn = root.insertFullName(parent);
	cn.addParent(pn);
    }

    public Set/*<String>*/ matchName(NamePattern pattern, String context) {
	PCNode context_node = root.insertFullName(context);
	Set/*<PCNode>*/ nodes = pattern.match(context_node);
	Set/*<String>*/ result = new HashSet();
	Iterator ni = nodes.iterator();
	while (ni.hasNext()) {
	    PCNode n = (PCNode)ni.next();
	    if (n.isClass()) {
		result.add(n.toString());
	    }
	}
	return result;
    }

    public boolean matchesClass(ClassnamePatternExpr pattern, String context, String cl) {
	PCNode context_node = root.insertFullName(context);
	PCNode cl_node = root.insertFullName(cl);
	return pattern.matches(context_node, cl_node);
    }

}

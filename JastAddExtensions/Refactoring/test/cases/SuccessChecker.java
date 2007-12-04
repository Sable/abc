package test.cases;

import java.util.Iterator;
import java.util.List;

import changes.ASTChange;

public abstract class SuccessChecker {
	
	public abstract boolean check(List res);

	public void dump_changes(List res) {
		System.out.println("proposed changes:");
		for(Iterator i=res.iterator();i.hasNext();) {
			System.out.println(((ASTChange)i.next()).prettyprint());
		}
	}

}

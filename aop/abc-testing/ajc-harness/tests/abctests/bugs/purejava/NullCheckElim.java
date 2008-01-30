import org.aspectj.testing.Tester;

/* checks NullCheckElim in NullCheckEliminatoor, resp. BranchedRefVarsAnalysis;
 * assignments of the form "o=o.field" were treated wrong
 */
public class NullCheckElim{

	NullCheckElim next;
	
	public static void main(String[] args) {
		try {
			new NullCheckElim().go();
		} finally {
			Tester.expectEvent("passed");
		}
	}
	
	void go() {
				
        int count = 0;

        for (NullCheckElim current = this; current != null;
                current = current.next, count++) {} //NullCheckElim will trigger a NullPointerException here
		
        Tester.event("passed");
	}
	
}
/* from the Eclipse test suite */

public class A {
    public void foo() {
	Object runnable= null;
	Object[] disposeList= null;
	// from
	for (int i=0; i < disposeList.length; i++) {
	    if (disposeList [i] == null) {
		disposeList [i] = runnable;
		return;
	    }
	}
	// to
    }
}
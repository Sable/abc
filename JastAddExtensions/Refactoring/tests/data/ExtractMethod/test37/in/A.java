/* from the Eclipse test suite (simplified) */

public class A {
    public void foo() {
	while (1 == 1) {
	    // from
	    if (false)
		break;
	    return;
	    // to
	}
	return;
    }
}
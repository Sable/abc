/* from the IntelliJ test suite */

class A {
    int f() {
        try {
            // from
	    int k = 0;
            return k;
	    // to
        } finally {
        }
    }
}
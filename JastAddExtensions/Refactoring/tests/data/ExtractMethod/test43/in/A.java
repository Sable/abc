/* from the Eclipse test suite */

class A {
    void m() {
	final int i = 42;
	// from
	Runnable run =
	    new Runnable() {
		public void run() {
		    System.out.println(i);
		}
	    };
	// to
	run.run();
    }
}
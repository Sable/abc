/* from the Eclipse test suite */

public class A {
    public volatile boolean flag;
    
    protected void foo() {
	int i= 0;
	/*[*/try {
	    if (flag)
		throwException();
	    i= 10;
	} catch (Exception e) {
	}/*]*/
	read(i);
    }
    
    private void read(int i) {
    }
    
    private void throwException() throws Exception {
	throw new Exception();
    }
}

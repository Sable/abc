public class A {
    void m(boolean b) {
	int x = 42;
	try {
	    // from
	    if(b) {
		x = 23;
		throw new Exception();
	    }
	    // to
	} catch(Exception e) {
	    System.out.println(x);
	}
    }

    public static void main(String[] args) {
        new A().m(true);
    }
}

//Listing 8.16 TestException.java

public class TestException {
    public static void main(String[] args) {
	BusinessClass bc = new BusinessClass();
	bc.businessMethod1();
	try {
	    bc.businessMethod2();
	} catch (BusinessException ex) {
	    // Do something...
	    // Log it, execute recovery mechanism, etc.
	    System.out.println("Caught:" + ex);
	}
    }
}

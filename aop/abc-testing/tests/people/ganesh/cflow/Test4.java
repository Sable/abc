public class Test4 {

    public static void main(String[] args) {
	new Test4();
	try {
	    throw new RuntimeException();
	} catch(RuntimeException e) {
	}
    }

}

aspect Test4Aspect {

    /*
    before(): execution(Test4.new()) 
	&& cflow(staticinitialization(Test4Aspect)) { }
    */

    before(): handler(RuntimeException) && cflow(handler(RuntimeException)) {
	System.out.println("handler");
    }
}

import java.io.PrintStream;

class Test {
    public static void main() {
        new Runnable() {
            public void run() {
                newMethod().println("Text");
            }
	    
	    // put method inside
	    private PrintStream newMethod() {
		return System.out;
	    }
        };
    }
}
// RenameVariable/test26/in/A.java p A f out
package p;

import java.io.PrintStream;
import static java.lang.System.*;

public class A {
    static PrintStream f = 
        new PrintStream(out) { 
            public void println(String s) {
	        super.println(42);
	    }
        };
    public static void main(String[] args) {
        out.println("23");
    }
}
//Listing 3.4 TestFactorial.java: factorial computation

import java.util.*;

public class TestFactorial {
    public static void main(String[] args) {
	System.out.println("Result: " + factorial(5) + "\n");
	System.out.println("Result: " + factorial(10) + "\n");
	System.out.println("Result: " + factorial(15) + "\n");
	System.out.println("Result: " + factorial(15) + "\n");
    }

    public static long factorial(int n) {
	if (n == 0) {
	    return 1;
	} else {
	    return n * factorial(n-1);
	}
    }
}

//Listing 8.9 TestSynchronous.java

import java.util.Vector;

public class TestSynchronous {
    public static void main(String[] args) {
	int intMax = Math.max(1, 2);
	System.out.println("intMax = " + intMax);
	double doubleMax = Math.max(3.0, 4.0);
	System.out.println("doubleMax = " + doubleMax);
	Vector v = new Vector();
	v.add(0, "AspectJ");
	Object str = v.get(0);
	System.out.println("str = " + str);
    }
}


// RenameMethod/test30/in/A.java p A m(int) valueOf
package p;

import static java.lang.String.*;

public class A {
	static String m(int i) { return "42"; }
	public static void main(String[] args) {
		System.out.println(String.valueOf(23));
	}
}
// Encapsulate/test8/in/A.java p A i
package p;

public class A {
    protected int i;
    public void m() {
	Object o = new Object() {
		int k = i;
	    };
    }
}

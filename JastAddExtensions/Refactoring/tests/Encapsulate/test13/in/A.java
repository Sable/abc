// Encapsulate/test13/in/A.java p A i
package p;

public class A {
    public static void main(String[] args) {
	new A().m();
    }

    int i;
    A[] a;
    void m() {
	a = new A[1];
	a[0] = new A();
	a[n()].i += 2;
    }
    int n() {
	System.out.println("here");
	return 0;
    }
}

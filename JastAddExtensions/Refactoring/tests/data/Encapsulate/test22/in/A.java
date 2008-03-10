// Encapsulate/test22/in/A.java p A i
package p;

aspect X {
    public int A.i;
}

public class A {
    public int f(int j) {
	return i + j;
    }
}

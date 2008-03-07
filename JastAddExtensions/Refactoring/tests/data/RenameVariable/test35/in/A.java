// RenameVariable/test35/in/A.java p A x y
package p;

class A {
    int x;
    class B {
	int y;
	public B() {
	    this(x);
	}
	public B(int z) {
	    System.out.println(z);
	}
    }
}

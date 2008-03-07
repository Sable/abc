// RenameVariable/test34/in/A.java p A f g
package p;

class A {
    static int f = 23;
    public A(int g) {
	this(f, 0);
    }
    public A(int x, int y) {
	System.out.println(x);
    }
}

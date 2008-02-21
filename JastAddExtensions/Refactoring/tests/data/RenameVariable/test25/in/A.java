// RenameVariable/test25/in/A.java p A x y
package p;

public class A {
    int x;
    void m(int[] ys) {
	for(int y : ys)
	    y=x;
    }
}
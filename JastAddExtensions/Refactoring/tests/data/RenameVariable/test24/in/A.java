// RenameVariable/test24/in/A.java p A x y
package p;

public class A {
    int x;
    void m() {
	for(int y=0;;++y)
	    y=x;
    }
}
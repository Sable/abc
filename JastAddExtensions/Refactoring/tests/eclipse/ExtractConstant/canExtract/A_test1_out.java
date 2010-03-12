//5, 16 -> 5, 21   AllowLoadtime == false
package p;
class A {
	void f() {
		int i= CONSTANT;
	}
	private static final int CONSTANT= 1 + 2;
}
//5, 20 -> 5, 27   AllowLoadtime == false
package p;
class A {
	void f() {
		boolean i= CONSTANT;
	}
	static boolean isRed(){
		return 5==1;
	}
	private static final boolean CONSTANT= isRed();
}
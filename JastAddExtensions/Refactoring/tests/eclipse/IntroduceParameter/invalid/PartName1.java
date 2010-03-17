//selection: 7, 11, 7, 13
package invalid;

class PartName1 {
	public static int foo() { return 17; }
	void bar() {
		int a= foo();
	}
	void use() {
		bar();
	}
}


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		A a = new A();
		System.out.println("Hello world!!!" + a);
		Z z = new A();
		z = new B();
	}
}

class A {}

class B {}

class Z {}
aspect X {
	declare parents : A || B extends Z;

	int A.i;

	void B.foo(){};
}

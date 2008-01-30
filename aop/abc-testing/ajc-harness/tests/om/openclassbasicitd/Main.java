
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		A a = new A();
		System.out.println("Hello world!!!" + a);
	}
}

class A {
}

interface I {
}

aspect X {
	int A.i;
	public void Main.foo() {
		System.out.println("Main.foo()");
	}
	public Main.new(int x, int y) {
	}

	public int I.j;
	public void I.foo() {
		System.out.println("I.foo()");
	}
	declare parents: A implements I;
	declare parents: Main implements I;
}

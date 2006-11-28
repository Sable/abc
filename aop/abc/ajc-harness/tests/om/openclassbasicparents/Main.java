
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		A a = new A();
		Main b = new B();
		System.out.println("Hello world!!!" + a);
	}
}

class A {}

class B {}

aspect X {
	declare parents : A extends Main;
	declare parents : B extends Main;
}

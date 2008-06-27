import pack.*;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		A a = new A();
		System.out.println("Hello world!!!" + a);
	}
}

class D {}

class E {}

aspect X{
	declare parents : Main implements I;
	declare parents : D implements I;
	declare parents : E implements I;
}

aspect Z{
	declare parents : B implements I;
}

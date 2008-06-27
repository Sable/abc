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
	int Main.i;
	int D.i;
	int E.i;
}

aspect Z{
	int B.i;
}

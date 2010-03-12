package p;

import java.io.IOException;

public class Foo {
	
	/**
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static void bar() throws IOException, ArrayIndexOutOfBoundsException {
		/*///Foo.*/foo();
	}

	public static void foo() throws IOException, ArrayIndexOutOfBoundsException {

	}

	void foo2() throws Exception {
		/*///Foo.*/bar();	// <- invoke here
	}

}

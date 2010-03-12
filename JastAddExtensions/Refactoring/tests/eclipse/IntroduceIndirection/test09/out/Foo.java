package p;

public class Foo {
	/**
	 * 
	 */
	public static void bar() {
		/*///Foo.*/foo();
	}

	// Test warnings for incorrectly qualified static calls
	
	static void foo() {	// <- invoke here (change name!)
		
	}
	
	Foo getFoo() {
		return new Foo();
	}
	
	{
		/*///Foo.*/bar();
		Foo.bar();
	}

}

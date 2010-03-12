package p;

public class Foo {

	/**
	 * @param bar
	 */
	public static void bar(Bar bar) {
		bar.getDisplay();
	}
	
	// Test correct "thisification".

	class X extends Bar {
		
		{
			/*///Foo.*/bar(this);
		}
	}
}

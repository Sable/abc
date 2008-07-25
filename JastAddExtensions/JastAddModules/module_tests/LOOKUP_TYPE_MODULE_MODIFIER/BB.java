module m2;
package b;
public class BB {
	public BB() {
		System.out.println(this.getClass());
	}
	BB(int x) {
		System.out.println(this.getClass());
	}

	module BB(long x) {
		System.out.println(this.getClass());
	}
	public void publicf() {
		System.out.println(this.getClass() + ".publicf()");
	}
	void packagef() {
		System.out.println(this.getClass() + ".packagef()");
	}
	protected void protectedf() {
		System.out.println(this.getClass() + ".protectedf()");
	}
	module void modulef() {
		System.out.println(this.getClass() + ".modulef()");
	}

	public static class BBPublic {}
	static class BBPackage {}
	module static class BBModule{}

	public class BBInnerPublic {}
	class BBInnerPackage {}
	module class BBInnerModule{}
}
class BBPackage {}
module class BBModule{}

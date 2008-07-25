module m1;
package a;
public class AA {
	public AA() {
		System.out.println(this.getClass());
	}

	AA(int x) {
		System.out.println(this.getClass());
	}

	module AA(long x) {
		System.out.println(this.getClass());
	}

	public void publicf() {}
	void packagef() {}
	protected void protectedf() {}
	module void modulef() {}

	public static class AAPublic {}
	static class AAPackage {}
	module static class AAModule{}

	public class AAInnerPublic {}
	class AAInnerPackage {}
	module class AAInnerModule{}
}
class AAPackage {}
module class AAModule{}

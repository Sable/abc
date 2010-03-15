package p;
class A {
	public static class B extends A {
		public void foo(){
		}
	}
	
	private final class Inner extends B {
	}

	static B b = new B() {
		public void foo() {
			B b = new Inner();
		}
	};
}

package p;
//private, static, final
class A{
	private final class Inner extends A {
		void f(){
			x();
		}
		private Inner(int i) {
			super(i);
		}
	}
	A(int i){
	}
	void f(){
		new Inner(1);
	}
	static void x(){}
}
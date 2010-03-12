package p;
//private, nonstatic, final
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
	void x(){}
}
package p;
//private, static, final
class A{
	private final class Inner extends A {
		int X; /// = 0;
		void f(){
		}
		private Inner(int i) {
			super(i);
			X = 0; ///
		}
	}
	A(int i){
	}
	void f(){
		new Inner(1);
	}
}
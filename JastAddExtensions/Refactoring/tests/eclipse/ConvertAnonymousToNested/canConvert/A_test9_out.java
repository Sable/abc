package p;
//private, nonstatic, final
class A{
	private final class Inner extends A {
		void f(){
			y= 0;
		}
		private Inner(int i) {
			super(i);
		}
	}
	int y;
	A(int i){
	}
	void f(){
		new Inner(1);
	}
}
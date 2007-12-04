package test;

public class TestSrc3 {
	
	static class X {
		static void m() { System.out.println("X.m"); }
		void n() { System.out.println("X.n"); }
	}
		
	static class Y extends X {
		static void m() { System.out.println("Y.m"); }
		void n() { System.out.println("Y.n"); }
		class Z {
			void n() { System.out.println("Z.n"); }
			void tst() { m(); n(); Y.this.m(); Y.this.n(); }
		}
		void tst() { super.n(); ((X)this).n(); }
	}
	
	public static void main(String[] args) {
		Y y = new Y();
		y.tst();
		Y.Z z = y.new Z();
		z.tst();
	}

}

import org.aspectj.testing.Tester;

aspect FlowIns {
	
	static int c_a = 0;
	static int c_b = 0;
	static int c_c = 0;
	static int c_d = 0;
	
	
	dependent before a(Object o): call(* a()) && target(o) { c_a++; };	//warning here, because we remove shadows for a
	dependent before b(Object o): call(* b()) && target(o) { c_b++; };
	dependent before c(Object o): call(* c()) && target(o) { c_c++; };	//warning here, because we remove shadows for c
	dependent before d(Object o): call(* d()) && target(o) { c_d++; };

	dependency {
		strong a,b; 	
		weak c;		
	}

	dependency {
		strong d; 	
		weak c;		
	}

	public static void main(String[] args) {
		Test t1 = new Test();
		t1.a();// can be removed because b() is never called on t1 
		t1.c();// can be removed because b() is never called on t1
		
		Test t2 = new Test();
		t2.a();//must stay
		t2.b();//must stay
		t2.c();//must stay
		Test t2_2 = t2;
		t2_2.c();//must stay
		
		Test t3 = new Test();
		t3.c();// can be removed because neither a() nor b() is never called on t3

		Test t4 = new Test();
		t4.c();//must stay because d() keeps it alive
		t4.d();//must stay
		
		Tester.check(c_a==1,"c_a should equal 1, is: "+c_a);
		Tester.check(c_b==1,"c_b should equal 1, is: "+c_b);
		Tester.check(c_c==3,"c_c should equal 3, is: "+c_c);
		Tester.check(c_d==1,"c_d should equal 1, is: "+c_d);
	}	
	
	static class Test {
	
		void a() {}
		void b() {}
		void c() {}
		void d() {}
		
	}
	
}
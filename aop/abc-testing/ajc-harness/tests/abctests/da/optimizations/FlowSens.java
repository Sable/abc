import org.aspectj.testing.Tester;

aspect FlowSens {
	
	static int c_a = 0;
	static int c_b = 0;
	static int c_c = 0;
	static int c_d = 0;
	
	
	dependent before a(Object o): call(* a()) && target(o) { c_a++; };	//warning here, because we remove shadows for a
	dependent before b(Object o): call(* b()) && target(o) { c_b++; };
	dependent before c(Object o): call(* c()) && target(o) { c_c++; };	//warning here, because we remove shadows for c
	dependent before d(Object o): call(* d()) && target(o) { c_d++; };

	//regex: a b* (b|c) d+
	dependency {
		a,b,c,d;
		initial s1:	a -> s2;
				s2: b -> s3,
					b -> s2,
					c -> s3;
				s3:	d -> s4;
		final s4: d -> s4;
	}

	public static void main(String[] args) {
		t1();
		t2();
		t3();
		t4();


		Tester.check(c_a==1,"c_a should equal 1, is: "+c_a);
		Tester.check(c_b==0,"c_b should equal 0, is: "+c_b);
		Tester.check(c_c==1,"c_c should equal 1, is: "+c_c);
		Tester.check(c_d==2,"c_d should equal 2, is: "+c_d);
	}	
	
	static void t1() {
		//flow-insensitive case
		Test t1 = new Test();
		t1.a();//can be removed (need d) 
		t1.c();//can be removed (need d)
	}
	
	static void t2() {
		//does not match (a seen twice)
		Test t2 = new Test();
		t2.a(); 
		t2.a(); 
		t2.c();
		t2.d();
	}
	
	static void t3() {
		//matches
		Test t3 = new Test();
		t3.a(); 
		t3.b();//do not need to read the b
		t3.c();
		t3.d();
		t3.d();
	}
	
	static void t4() {
		//does not match (one c to many)
		Test t4 = new Test();
		t4.a(); 
		t4.c();
		t4.c();
		t4.d();
	}
	
	static class Test {
	
		void a() {}
		void b() {}
		void c() {}
		void d() {}
		
	}
	
}
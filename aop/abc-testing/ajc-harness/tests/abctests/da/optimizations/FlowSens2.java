import org.aspectj.testing.Tester;

aspect FlowSens2 {
	
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
		//flow-insensitive case
		Test t1 = new Test();
		t1.a();//can be removed (need d) 
		t1.c();//can be removed (need d)

		Test t2 = new Test();//can be removed (one a too many)
		t2.a(); 
		t2.a(); 
		t2.c();
		t2.d();

		Test t3 = new Test();//matches
		t3.a(); 
		t3.b();//do not need to read the b, so in principle this shadow can be removed; however, we fail to do so because we would have to do backwards-determinization of the state machine, which right now we don't 
		t3.c();
		t3.d();
		t3.d();

		Test t4 = new Test();//can be removed (one c too many)
		t4.a(); 
		t4.c();
		t4.c();
		t4.d();


		Tester.check(c_a==1,"c_a should equal 1, is: "+c_a);
		Tester.check(c_b==0,"c_b should equal 0, is: "+c_b);
		Tester.check(c_c==1,"c_c should equal 1, is: "+c_c);
		Tester.check(c_d==2,"c_d should equal 2, is: "+c_d);
	}	

	static class Test {
	
		void a() {}
		void b() {}
		void c() {}
		void d() {}
		
	}
	
}
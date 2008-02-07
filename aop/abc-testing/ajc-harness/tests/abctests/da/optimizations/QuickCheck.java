aspect QuickCheck {
	
	dependent before a(): call(* a()) {};
	dependent before b(): call(* b()) {};
	dependent before c(): call(* c()) {};
	dependent before d(): call(* d()) {};	//warning will go here
	dependent before e(): call(* e()) {};
	dependent before f(): call(* f()) {};
	dependent before g(): call(* g()) {};	//warning will go here

	//fulfilled
	dependency {
		strong a; 	//matches, keeps b alive
		weak b;		
	}

	//not fulfilled
	dependency {
		strong c; 	//never matches and d is in no other dependency => d can go
		weak d;		
	}

	//not fulfilled
	dependency {
		strong e; 	//never matches, but...
		weak f;		
	}

	//fulfilled
	dependency {
		strong b; //... b matches and will keep f alive
		weak f;		
	}

	//not fulfilled
	dependency {
		strong b,c; //b matches but c never matches; g is in no other dependency => g can go
		weak g;		
	}

	void driver() {
		a();//
		b();// all three advice a,b,f need to be kept alive
		f();//
		
		d();// d can go
		g();// g can go as well
	}
	
	void a() {}
	void b() {}
	void c() {}
	void d() {}
	void e() {}
	void f() {}
	void g() {}
	
}
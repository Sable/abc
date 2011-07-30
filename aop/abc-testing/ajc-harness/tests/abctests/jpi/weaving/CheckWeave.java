
jpi void JP();
jpi void JP1() extends JP();
jpi void JP2() extends JP();
jpi void J1() extends JP1();
jpi void J2() extends JP2();
jpi void A1() extends J2();
jpi void A2() extends J2();

class C{
	exhibits void JP1() : call(* foo());
}

class X{
	exhibits void JP1() : call(* foo(..)) && within(X);
}

class Q{
	exhibits void JP1() : execution(* bar());
	exhibits void J1() : call(* bar(..)) && within(Q);
}

aspect A{
	
	exhibits void JP1() : cflow(execution(* foo()));

	
	void around JP1(){
		
	}
	
	void around J1(){}
	/*
	void around JP1(){}
	
	void around JP2(){}
	
	void around A1(){}
	*/

}

jpi void H();
jpi void JP() extends H();
jpi void JP1() extends JP();
jpi void JP2() extends JP();

jpi void AJP();

class Z{
	exhibits void JP() : call(* foo());
	exhibits void H() : call(* *.*(..));
}

class W{
	exhibits void JP1() : call(* foo(..)) && within(W);
}

class X{
	exhibits void JP2() : call(* foo(..)) && within(X);
}

aspect A{
	exhibits void AJP() : execution(* zar(..));
	
	void around H(){}
	
	void around JP(){}
	
	void around JP1(){}

	void around JP2(){}
	
	void around AJP(){}
}

aspect B{
	void around H(){}	
	
	void around JP(){}
	
	void around JP1(){}
}

aspect C{

	void around H(){}
	
	void around JP(){}
	
	void around JP2(){}
}

aspect D{
	void around JP(){}

	void around H(){}
}

/*

aspect z{
	void around() : call(* foo()) && within(z){}
}

*/
public aspect CheckCJPDeclared {
	
	void foo() {
		exhibit JP {};		//error: not declared
		exhibit Object {};	//error: not a joinpoint type
		exhibit Exception {};	//correct
		exhibit JP2 {};			//correct
		exhibit CheckCJPDeclared.JP2 {};	//correct		
	}
	
	joinpoint void JP2();

	joinpoint void Exception();
	
	after JP2() {} //ok

	after JP() {} //error: not declared
}
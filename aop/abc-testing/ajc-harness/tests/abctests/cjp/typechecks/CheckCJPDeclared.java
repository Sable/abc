public aspect CheckCJPDeclared {
	
	void foo() {
		exhibit JP {};		//error: not declared
		exhibit Object {};	//error: not a jpi type
		exhibit Exception {};	//correct
		exhibit JP2 {};			//correct
		exhibit CheckCJPDeclared.JP2 {};	//correct		
	}
	
	jpi void JP2();

	jpi void Exception();
	
	after JP2() {} //ok

	after JP() {} //error: not declared
}
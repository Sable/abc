public aspect CheckFinalAccess {
	
	void nonFinalAccess(int i) {
		exhibit JP { i++; }; //error because i is not final
	}
	
	void finalWrite(final int i) {
		exhibit JP { i++; }; //error because i is written
	}
	
	void finalRead(final int i) {
		exhibit JP { int j = i; }; 	//no error
	}
	
	void nonFinalAccessLocal() {
		int i=0;
		exhibit JP { i++; }; //error because i is not final
	}

	void finalWriteLocal() {
		final int i=0;
		exhibit JP { i++; }; //error because i is written
	}
	
	void finalReadLocal() {
		final int i=0;
		exhibit JP { int j = i; }; 	//no error
	}

	joinpoint void JP();
}
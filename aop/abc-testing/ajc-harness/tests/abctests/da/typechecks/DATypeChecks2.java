aspect DATypeChecks2 {
	
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};

	/*
	 * T1:
	 * Same advice name twice. An error should be issued.
	 */	
	dependency {
		strong a1,a1;
	}	
		
	/*
	 * T2:
	 * Same advice name twice. An error should be issued.
	 */	
	dependency {
		strong a1;
		weak a1;
	}	
}
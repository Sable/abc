aspect DATypeChecks3 {
	
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};

	/*
	 * T1:
	 * Refer to non-existing advice name. An error should be issued.
	 */	
	dependency {
		strong a1;
		weak a2;
	}	
		
	/*
	 * T1:
	 * Refer to non-existing advice name. An error should be issued.
	 */	
	dependency {
		strong a2;
		weak a1;
	}
	
}
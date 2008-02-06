aspect DATypeChecks4 {
	
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};
	dependent after  a2(Object o) returning(Object p): call(* foo()) && target(o) {};

	/*
	 * T1:
	 * Orphan advice "a2". An error should be issued.
	 */	
	dependency {
		strong a1;
	}	
		
}
aspect DATypeChecks5 {
	
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};

	/*
	 * T1:
	 * Duplicate advice name "a1". An error should be issued.
	 */	
	dependency {
		strong a1;
	}	
}
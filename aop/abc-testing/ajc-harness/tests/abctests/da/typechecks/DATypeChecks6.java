aspect DATypeChecks6 {
	
	/*
	 * T1:
	 * Named advice that is not a dependent advice. Should give an error.
	 */	
	after a1(Object o) returning(Object p): call(* foo()) && target(o) {};
}
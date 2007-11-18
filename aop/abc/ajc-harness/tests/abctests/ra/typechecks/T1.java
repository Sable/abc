/* 
 * checks that a relational advice cannot be declared
 * in a non-relational aspect
 */
aspect T1 {
	pointcut nothing();
	relational before(): nothing() {}	
}

/* 
 * checks that a a non-relational aspect
 * cannot take parameters
 */
aspect T2(Object a) {
	
}

/*
 * checks that the relational aspect
 * parameter list may be empty
 */
relational aspect T3() {}

/*
 * checks that relational aspects may extend
 * others if they have the same formal
 * parameter list
 */
abstract relational aspect T4_A(String s) {}
relational aspect T4_B(String t) extends T4_A{}

/*
 * check that inconsistent parameter lists
 * are captured (1)
 */
abstract relational aspect T5_A(String s) {}
relational aspect T5_B() extends T5_A{}

/*
 * check that fields with the same name may be declared
 */
relational aspect T6(String s) {
	int s;	
}

/*
 * check that aspect parameters with the same name
 * may be declared 
 */
relational aspect T7(String s) {
	before(String s): let(s,"") {}	
	relational before(String s): let(s,"") {}	
}

/*
 * check that relational advice can access
 * aspect parameters in body
 */
relational aspect T9(String s) {
	pointcut nothing();
	relational before(): nothing() {
		System.out.println(s);
	}	
}

/*
 * check that relational advice can access
 * aspect parameters in pointcut
 */
relational aspect T10(String s) {
	relational before(): args(s) {	}	
}


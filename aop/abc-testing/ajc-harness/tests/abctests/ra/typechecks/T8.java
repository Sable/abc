/*
 * check that non-relational advice cannot access
 * aspect parameters
 */
relational aspect T8(String s) {
	pointcut nothing();
	before(): nothing() {
		System.out.println(s);
	}	
}
aspect AspectB {
    public static int callCtr = 0;
	pointcut pc() : call(* test(..));

	before() : pc() {
	    callCtr++;
		System.out.println("AspectB");
	}
}

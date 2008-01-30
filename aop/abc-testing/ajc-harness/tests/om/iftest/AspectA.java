aspect AspectA {
    public static int callCtr = 0;
	pointcut pc() : call(* test(..));

	before() : pc() {
		System.out.println("AspectA");
		callCtr++;
	}
}

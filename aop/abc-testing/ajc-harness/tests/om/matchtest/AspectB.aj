aspect AspectB {
    public static int aaCallCtr = 0;
    public static int abCallCtr = 0;
    public static int baCallCtr = 0;
    public static int bbCallCtr = 0;

    
	pointcut aa(): call(* MatchTestA.a(..));
	pointcut ab(): call(* MatchTestA.b(..));
	pointcut ba(): call(* MatchTestB.a(..));
	pointcut bb(): call(* MatchTestB.b(..));

	before() : aa() {
		System.out.println("Before MatchTestA.a() from AspectB");
		aaCallCtr++;
	}

	before() : ab() {
		System.out.println("Before MatchTestA.b() from AspectB");
		abCallCtr++;
	}

	before() : ba() {
		System.out.println("Before MatchTestB.a() from AspectB");
		baCallCtr++;
	}

	before() : bb() {
		System.out.println("Before MatchTestB.b() from AspectB");
		bbCallCtr++;
	}
}

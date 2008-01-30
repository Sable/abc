aspect AspectB {
	pointcut a() : AspectA.allfuncs();

	after() : a() {
		System.out.println("After call " + thisJoinPoint.getSignature() + ", from AspectB");
	}
}

module FibMod {
	class A;
	aspect FibAspect;
	__sig {
		pointcut get(int A.prevX);
		pointcut set(int A.prevX);
		method int fib(int);
	}
}

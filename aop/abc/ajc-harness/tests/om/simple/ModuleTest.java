module ModuleTest {
	//This is a test
	class A;
	aspect AspectA;
	module ModuleTest2;
	__sig {
		pointcut call(* *(..));
		pointcut AspectA.allfuncs();
		method int A.fib(x);
	}
}

module ModuleTest {
	//This is a test
	class A;
	friend AspectA;
	open ModuleTest2;
	expose : call(* *(..));
	expose : AspectA.allfuncs();
	advertise : call(int A.fib(x));
}

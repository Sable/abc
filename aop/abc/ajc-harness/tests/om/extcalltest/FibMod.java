module FibMod {
	class A;
	friend FibAspect;
	
	expose : get(int A.prevX);
	expose : set(int A.prevX);
	advertise : call(int fib(int));
}

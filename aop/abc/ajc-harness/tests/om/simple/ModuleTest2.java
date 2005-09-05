module ModuleTest2 {
	aspect AspectB;
	module ModuleTest3;
	__sig {
		pointcut call(* *(..));
	}
}

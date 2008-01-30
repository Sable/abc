//test for bug 81 (wrong pointcut precedence w.r.t. || and &&)

aspect PointcutPrecedence {
	
	after(Object o) :
		call(* *(..)) && target(o) || 	//this should compile fine
		call(* *(..)) && args(o){		//
		//body does not matter
	}
}
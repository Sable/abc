aspect ContainsTest {
	pointcut pc1() : contains(call(* *(..)));//should be ok
	pointcut pc() : contains(call(* *(..)) && args(A));//should fail
	pointcut pc1() : contains(cflow(call(* *(..))));//should fail
}

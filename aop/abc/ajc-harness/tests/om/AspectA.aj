import org.aspectj.lang.*;

aspect AspectA {
	pointcut allfuncs(): call(* *.*(..));

	before() : allfuncs() {
		System.out.println("Before call " + thisJoinPoint.getSignature() + 
				", from Test");
	}
	
	void Another.intertypeMethod() {
		System.out.println("This is an intertype method");
	}
}

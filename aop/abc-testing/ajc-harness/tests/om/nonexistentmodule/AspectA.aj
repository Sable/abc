import org.aspectj.lang.*;

aspect AspectA {
	pointcut allfuncs(): call(* *.*(..)) && !within(AspectA) && !within(AspectB);

	before() : allfuncs() {
		System.out.println("Before call " + thisJoinPoint.getSignature() + 
				", from AspectA");
	}
	
	void Another.intertypeMethod() {
		System.out.println("This is an intertype method");
	}
}

import org.aspectj.testing.Tester;

public class CheckTargetExposure {
	
	void instanceMethod() {
		exhibit A.JP { }; 
	}

	public static void main(String[] args) {
		new CheckTargetExposure().instanceMethod();
	}
	
}

aspect A {
	
	joinpoint void JP();
	
	void around JP() {
		Tester.check(thisJoinPoint.getTarget()==null,"getTarget() failed to return null, returned instead: "+thisJoinPoint.getTarget());
	}
	
	
}
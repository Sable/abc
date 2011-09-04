import org.aspectj.testing.Tester;

public class CheckTargetExposure {
	
	static CheckTargetExposure f;
	
	void instanceMethod() {
		exhibit A.JP { }; 
	}

	static void staticMethod() {
		exhibit A.JP2 { }; 
	}
	
	public static void main(String[] args) {
		f = new CheckTargetExposure();
		f.instanceMethod();
		staticMethod();
	}
	
}

aspect A {
	
	jpi void JP();
	
	jpi void JP2();

	void around JP() {
		Tester.check(thisJoinPoint.getTarget()==CheckTargetExposure.f && thisJoinPoint.getTarget()!=null,"getTarget() returned null or wrong object");
	}
	
	void around JP2() {
		Tester.check(thisJoinPoint.getTarget()==null,"getTarget() failed to return null, returned instead: "+thisJoinPoint.getTarget());
	}
	
	
}
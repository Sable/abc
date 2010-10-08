import org.aspectj.testing.Tester;

public class CheckThisExposure {
	
	static CheckThisExposure f;
	
	void instanceMethod() {
		exhibit A.JP { }; 
	}

	static void staticMethod() {
		exhibit A.JP2 { }; 
	}
	
	public static void main(String[] args) {
		f = new CheckThisExposure();
		f.instanceMethod();
		staticMethod();
	}
	
}

aspect A {
	
	joinpoint void JP();
	
	joinpoint void JP2();

	void around JP() {
		Tester.check(thisJoinPoint.getThis()==CheckThisExposure.f && thisJoinPoint.getThis()!=null,"getThis() returned null or wrong object");
	}
	
	void around JP2() {
		Tester.check(thisJoinPoint.getThis()==null,"getThis() failed to return null, returned instead: "+thisJoinPoint.getThis());
	}
	
	
}
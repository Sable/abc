import org.aspectj.testing.Tester;

public class CheckArgsExposure {
	
	static Object o, p;
	
	void instanceMethod() {
		o = new Object();
		p = new Object();
		exhibit A.JP { }; 
	}

	public static void main(String[] args) {
		new CheckArgsExposure().instanceMethod();
	}
	
}

aspect A {
	
	joinpoint void JP(Object o, Object p);
	
	void around JP(Object a, Object b) {
		Tester.check(thisJoinPoint.getArgs()[0]!=a,"getArgs()[0] did not return 1st argument value, instead:"+thisJoinPoint.getArgs()[0]);
		Tester.check(thisJoinPoint.getArgs()[1]!=b,"getArgs()[1] did not return 2nd argument value, instead:"+thisJoinPoint.getArgs()[1]);
		Tester.check(thisJoinPoint.getArgs()[0]!=CheckArgsExposure.o,"getArgs()[0] did not return correct value, instead:"+thisJoinPoint.getArgs()[0]);
		Tester.check(thisJoinPoint.getArgs()[1]!=CheckArgsExposure.p,"getArgs()[1] did not return correct value, instead:"+thisJoinPoint.getArgs()[1]);
	}
	
	
}
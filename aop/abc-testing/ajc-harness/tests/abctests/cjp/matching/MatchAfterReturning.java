import org.aspectj.testing.Tester;

public aspect MatchAfterReturning {
	
	static Object o;
	
	public static void main(String args[]) {		
		Object ret = exhibit JP { return foo(); };
		Tester.check(o==ret, "after-returning advice captured incorrect value: "+o);
	}
	
	static Object foo() { return new Object(); }
	
	joinpoint Object JP(); 
	
	after JP() returning(Object r) {
		o = r;
	}	
}
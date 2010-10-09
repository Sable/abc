import org.aspectj.testing.Tester;

public aspect MatchAfterThrowing {
	
	static Exception e;
	
	public static void main(String args[]) {
		Exception caught = null;
		try {			
			exhibit JP { throw new Exception("test"); };
		} catch(Exception e) {
			caught=e;
		}
		Tester.check(caught==e, "after-throwing advice captured incorrect value: "+e);
	}
	
	static Object foo() { return new Object(); }
	
	joinpoint void JP() throws Exception; 
	
	after JP() throwing(Exception r) {
		e = r;
	}	
}
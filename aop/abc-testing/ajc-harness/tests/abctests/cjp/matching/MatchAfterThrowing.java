import org.aspectj.testing.Tester;
import java.io.*;

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
	
	jpi void JP() throws Exception; 
	
	after JP() throwing(Exception r) { //ok, since we relax type-checking checks.
		e = r;
	}	

	after JP() throwing(IOException r) { //ok, since we relax type-checking checks.
		Tester.check(false,"executed wrong piece of advice");
	}
}
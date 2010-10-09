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
	
	joinpoint void JP() throws Exception; 
	
	after JP() throwing(Exception r) {
		e = r;
	}	

	after JP() throwing(IOException r) { //should not execute, as an Exception is no IOException
		Tester.check(false,"executed wrong piece of advice");
	}
}
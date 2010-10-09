import org.aspectj.testing.Tester;
import java.util.*;

public aspect MatchAfterReturning {
	
	static HashMap o;
	
	public static void main(String args[]) {		
		HashMap ret = exhibit JP { return foo(); };
		Tester.check(o==ret, "after-returning advice captured incorrect value: "+o);
	}
	
	static HashMap foo() { return new HashMap(); }
	
	joinpoint HashMap JP(); 
	
	after JP() returning(HashMap r) {
		o = r;
	}	

	after JP() returning(List l) { //should never execute because a HashMap is no List
		Tester.check(false,"executed wrong piece of advice");
	}	
}
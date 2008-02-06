import org.aspectj.testing.Tester;

public class NotShared {

	static int i;
	
	public static void main(String[] args) {
		i = 0;
    	Tester.check(Logger.counter==0,"expected 0 match but saw "+Logger.counter);
	}
	
}

aspect Logger {
	
	static int counter = 0;
	
	after(): set(* *) && !within(Logger) && maybeShared() {
		counter++;
	}
	
}

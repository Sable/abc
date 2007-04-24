import org.aspectj.testing.Tester;

public aspect SyncMethods {

	static String s = "";
	
	synchronized static int foo(int x) {
		return 3+x;
	}
	
	before(): execution(* *(..)) {
		//does not matter; just exists to generate warning about restructuring
	}
	
	before(): lock() {
		s += "l";
    }
	
	before(): unlock() {
		s += "u";
    }
	
	public static void main(String[] args) {
		foo(1);
		Tester.checkEqual(s,"lu","Invalid sequence of locks and unlocks: "+s+" (should be lu)");
	}


}

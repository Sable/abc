import org.aspectj.testing.Tester;

public aspect DontMatchExecution {
	
	static int count = 0;
	
	joinpoint void Bar();
	
	public static void main(String args[]) {		
		foo();
		//expect two matches, one for main and one for foo
		Tester.checkEqual(count, 2, "expected 2 matches but saw "+count);
	}
	
	public static void foo() {
		exhibit Bar(){}();		
	}
	
	before(): execution(* *(..)) {
		count++;
	}
	
}
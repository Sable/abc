import org.aspectj.testing.Tester;

public aspect MatchCJP {
	
	static int count = 0;
	
	joinpoint void Bar();
	
	public static void main(String args[]) {		
		foo();
		Tester.checkEqual(count, 3, "expected 3 matches but saw "+count);
	}
	
	public static void foo() {
		for(int i=0;i<3;i++)
			exhibit Bar(){}();		
	}
	
	before Bar() {
		count++;
	}
	
}
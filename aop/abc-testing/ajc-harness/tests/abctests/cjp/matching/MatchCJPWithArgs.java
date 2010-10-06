import org.aspectj.testing.Tester;

public aspect MatchCJPWithArgs {
	
	static int count = 0;
	
	joinpoint void Bar(int i);
	
	public static void main(String args[]) {		
		foo();
		Tester.checkEqual(count, 6, "expected 6 matches but saw "+count);
	}
	
	public static void foo() {
		for(int i=1;i<4;i++)
			exhibit Bar(int j){}(i);		
	}
	
	before Bar(int k) {
		count+=k;
	}
	
}
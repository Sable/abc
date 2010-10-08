import org.aspectj.testing.Tester;

public aspect SimpleSyntax {
	
	static int count = 0;
	static int count2 = 0;
	
	joinpoint void Bar();
	
	public static void main(String args[]) {		
		foo();
		Tester.checkEqual(count, 3, "count: expected 3 matches but saw "+count);
		Tester.checkEqual(count2, 3, "count2: expected 3 matches but saw "+count);
	}
	
	public static void foo() {
		for(int i=0;i<3;i++)
			exhibit Bar{ count2++; };		
	}
	
	before Bar() {
		count++;
	}
	
}
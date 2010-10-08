import org.aspectj.testing.Tester;

public aspect Match2CJPWithDifferentArgs {
	
	static int count = 0;
	
	joinpoint void Bar(int i);
	
	public static void main(String args[]) {		
		foo();
		Tester.checkEqual(count, 11, "expected 11 matches but saw "+count);
	}
	
	public static void foo() {
		for(int i=1;i<4;i++)
			exhibit Bar(int j){}(i);		
		exhibit Bar(int s){s++;}(5);			
	}
	
	before Bar(int k) {
		count+=k;
	}
	
}
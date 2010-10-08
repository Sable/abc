import org.aspectj.testing.Tester;

public aspect Match2CJP {
	
	static int count = 0;
	
	joinpoint void Bar();
	
	public static void main(String args[]) {		
		foo();
		Tester.checkEqual(count, 4, "expected 4 matches but saw "+count);
	}
	
	public static void foo() {
		for(int i=0;i<3;i++)
			exhibit Bar(){}();		
		exhibit Bar(){}();		
	}
	
	before Bar() {
		count++;
	}
	
}
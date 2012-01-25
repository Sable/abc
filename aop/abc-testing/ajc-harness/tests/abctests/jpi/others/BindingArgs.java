import org.aspectj.testing.Tester;


jpi void JP(int a, int d);

aspect A{
	
	exhibits void JP(int a, int d) : call (* foo(..)) && args(a,d);
	public static int count = 1;
	
	public static void foo(int z, int l){
        Tester.check(true,"");		
	}

	before JP(int a, int d){
		Tester.check(2 == count++, "before I saw"+(count-1));
	}
	
	
	after JP(int a, int d){
		Tester.check(true, "");
		Tester.check(3 == count++, "I saw"+(count-1));		
	}
	
	void around JP(int a, int d){
		Tester.check(1 == count++, "around I saw"+(count-1));        
        proceed(a,d);
	}
	
	public static void main(String[] args){
		foo(5,6);
	}
}
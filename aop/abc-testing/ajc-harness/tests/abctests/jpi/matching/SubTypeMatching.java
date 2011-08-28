import org.aspectj.testing.Tester;

jpi void JP();
jpi void JP1(int b) extends JP();

public aspect A{
	
	exhibits void JP1(int b) : call(* *(..)) && args(b);
	
	public static void foo(int b){ }
	public static void main(String[] args){
		foo(5);
	}
	
	before JP(){
		Tester.check(false,"");
	}
	
	before JP1(int b){
		Tester.check(true,"");		
	}
	
	after JP1(int b){
		Tester.check(true,"");		
	}
	
}

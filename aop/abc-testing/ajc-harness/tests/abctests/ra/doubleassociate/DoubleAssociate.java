import org.aspectj.testing.Tester;

relational aspect DoubleAssociate(){
	
	static String s = "";
	
	relational before(): call(* hook(..)) {
		s +="x";
	}
	
	public static void main(String[] args) {
		associate();
		associate();//should be ignored
		
		hook();
		
		Tester.check(s.equals("x"),"output should be 'x' but is '"+s+"'");
	}
	
	static void hook(){};
	
	static class Foo {
		
	}
	
}
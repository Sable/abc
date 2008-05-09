import org.aspectj.testing.Tester;

relational aspect DoubleAssociate(){
	
	static String s = "";
	
	relational void before(): call(* hook(..)) {
		s +="x";
	}
		
	public static void main(String[] args) {
		hook();
		associate();
		associate();
		release();
		hook();
		associate();
		associate();//should be ignored
		hook();
		
		Tester.check(s.equals("x"),"output should be 'x' but is '"+s+"'");
	}
	
	after() returning(Object o): call(* associate()) {
		System.err.println("associated: "+o);
	}
	
	static void hook(){};
	
	static class Foo {
		
	}
	
}
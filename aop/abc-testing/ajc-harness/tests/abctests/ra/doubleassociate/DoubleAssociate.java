import org.aspectj.testing.Tester;

relational aspect DoubleAssociate(){
	
	static String s = "";
	
	relational void around(): call(* hook(..)) {
		s +="x";
	}
	
//  //This is equivalent to: 	
//	
//	void tracematch(DoubleAssociate a)  {
//		sym start before: execution(* main(..));
//		sym associate after returning(a): call(* associate());
//		sym associate_again after returning: call(* associate());
//		sym release after returning: call(* release());
//		sym action before: call(* hook());
//		sym action2 around: call(* hook());
//		(start | release) action* associate (associate_again* action)* associate_again* action2 {
//			s +="x";
//		}		
//	}
		
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
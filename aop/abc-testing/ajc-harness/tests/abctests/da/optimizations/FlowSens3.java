public class FlowSens3 {
	
	public static void main(String[] args) {
		f1();
	}
	
	static void f1() {
		FlowSens3 f = new FlowSens3();
		f.a(); 	//matches; needs to stay
		f.b(); 	//matches; needs to stay
		f.a(); 	//can be removed
	}
	
	void a(){}
	void b(){}
	
}

aspect Foo {
	
	dependent before a(Object o): call(* *.a()) && target(o) {
		
	}
	
	dependent before b(Object o): call(* *.b()) && target(o) {
		
	}
	
	dependency {
		a,b;
		initial s1: a -> s2;
				s2: b -> s3;
		final s3;
	}
	
}
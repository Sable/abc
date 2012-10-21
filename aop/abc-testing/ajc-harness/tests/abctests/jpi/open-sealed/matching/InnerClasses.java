import org.aspectj.testing.Tester;

global jpi void JP1() : execution(void *.foo());

Sealed class AA {
	private class BB {
		public void foo() {
			System.out.println("foo-bb");
		}
	}
	
	public void bar() {
		BB b = this.new BB();
		b.foo();
	}
}

Open class CC {
	private class DD {
		public void foo(){
			System.out.println("foo-dd");
		}
	}
	
	public void bar() {
		DD d = this.new DD();
		d.foo();
	}
}

aspect InnerClasses {
	
	public static int executionCounter = 0;
	
	void around JP1() {
		InnerClasses.executionCounter++;
		proceed();
	}
	
	public static void main(String[] args) {
		AA a = new AA();
		a.bar();
		CC c = new CC();
		c.bar();
		
		Tester.checkEqual(InnerClasses.executionCounter,1, "expected 1 matches but saw "+InnerClasses.executionCounter);		
		
	}
}
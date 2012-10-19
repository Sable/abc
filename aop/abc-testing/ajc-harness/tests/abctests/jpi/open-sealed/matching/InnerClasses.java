
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
	
	void around JP1() {
		System.out.println("hello");
		proceed();
	}
	
	public static void main(String[] args) {
		AA a = new AA();
		a.bar();
		CC c = new CC();
		c.bar();
	}
}
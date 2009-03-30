public class Tainting extends AbstractTest {
	
	//tracematch: a+ b+ c+
	
	static Tainting t = new Tainting();
	
	public static void main(String[] args) {
		f1();
		f2();
	}	
	
	static void f1() {
		t.a();	//may not be removed!
	}
	
	static void f2() {
		t.b();	//may not be removed!
		bad();
		t.x();	//cannot be removed: consider a call sequence f1(), f2() f3(): removing x would trigger the monitor only twice! 		
	}
	
	static void bad() {
		t.c(); //may not be removed!
	}
	
	
	
}
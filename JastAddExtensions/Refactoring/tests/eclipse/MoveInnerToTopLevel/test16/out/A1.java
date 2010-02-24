package p1;
import p.A;
///import p.Inner;
public class A1 {
	static void f(){
		p.///
		Inner i;
		p.///
		Inner.foo();
		p.///
		Inner.t =  2;
		p.Inner.foo();
		p.Inner.t =  2;
		A a;
	}

}
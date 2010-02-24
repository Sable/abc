package p;
class A {
	static void f(){
		Inner i;
		p.///
		Inner i2;
		Inner.foo();
		Inner.t =  2;
		p.///
		Inner.foo();
		p.///
		Inner.t =  2;
		p.Inner.foo();
		p.Inner.t =  2;
	}
}
module m1;
import a.AA;
import a.a.AAA;
public class A{
	a.AA aa = new a.AA();
	a.a.AAA aaa = new a.a.AAA();
	AA aa2 = new AA();
	AAA aaa2 = new AAA();
	
	m2::A a2 = new m2::A();
	m2::b.BB bb = new m2::b.BB();
	m2::b.bb.BBB bbb = new m2::b.bb.BBB();
	m2alias::b.bb.BBB bbb2 = new m2alias::b.bb.BBB();

	m3::c.cc.CCC ccc = new m3::c.cc.CCC();
	m2::m3::c.CC cc = new m2::m3::c.CC();
	m2alias::m3::c.CC cc2 = new m2alias::m3::c.CC();
	public A() {
		System.out.println(this.getClass());
	}
}

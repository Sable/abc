module m1;
public class A {
	a.AAPackage aapackage = new a.AAPackage();
	m2::b.BBModule bbmodule = new m2::b.BBModule();

	m2::b.BB bb = new m2::b.BB();
	m2::b.BB bb2 = new m2::b.BB(1);
	m2::b.BB bb3 = new m2::b.BB(1L);

	public A() {
		System.out.println(this.getClass());
		bb.modulef();
		bb.publicf();
		bb.packagef();
	}
}

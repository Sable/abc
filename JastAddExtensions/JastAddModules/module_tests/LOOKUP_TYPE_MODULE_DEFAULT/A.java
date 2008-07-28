module m1;
public class A{
	pack.P p1 = new pack.P();
	::pack.P p2 = new ::pack.P();
	//should both be ::java.lang.String, due to the special case for type lookup
	java.lang.String s1 = new java.lang.String();
	::java.lang.String s2 = new ::java.lang.String();
	public A() {
		System.out.println(this.getClass());
		System.out.println(s1.getClass());
		System.out.println(s2.getClass());
	}
}

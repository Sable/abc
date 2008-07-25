module m1;
public class A{
	java.lang.String s1 = new java.lang.String();
	::java.lang.String s2 = new ::java.lang.String();
	public A() {
		System.out.println(this.getClass());
		System.out.println(s1.getClass());
		System.out.println(s2.getClass());
	}
}

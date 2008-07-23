module m2;
public class B{
	m3.C c1 = new m3.C(); //should lookup to m3.C (the non module class)
	public B() {
		System.out.println(this.getClass());
	}
}

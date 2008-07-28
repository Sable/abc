module m1;
public class A {
	public AType atype = null;
	public BType btype = null;
	public CType ctype = null;
	public A() {
		btype = new BType();
		ctype = new CType();
		atype = new AType("A_AType", btype, ctype);
	}
}

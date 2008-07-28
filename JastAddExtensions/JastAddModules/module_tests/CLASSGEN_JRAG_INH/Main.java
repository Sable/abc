public class Main {
	public static void main(String args[]) {
		m1.m2.BType btype = new m1.m2.BType();
		m1.m3.CType ctype = new m1.m3.CType();
		m1.AType atype = new m1.AType("Main_AType", btype, ctype);
		m1.A a = new m1.A();

		m1.AType2 atype2 = new m1.AType2(new m1.AType3(new m1.AType4()));

		System.out.println(atype2.functionA2());
		System.out.println(atype2.getAType3().functionA2());

		System.out.println(atype.functionA());
		System.out.println(atype.getBType().functionA());
		System.out.println(atype.getCType().functionA());
	}
	public int f$b() {
		return 1;
	}
}

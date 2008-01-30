public class FieldInit {
    public static final int x=foo() ? bar(): 2;
    public static final int y=!foo() ? 2: bar();

    static boolean foo() {
	return false;
    }
    static int bar() { return 3; }
    public static void main(String[] args) {
    }

}

aspect FIAspect {
    before() : set(int FieldInit.*) { System.out.println(thisJoinPointStaticPart); }
}
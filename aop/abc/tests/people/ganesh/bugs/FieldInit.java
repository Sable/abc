public class FieldInit {
    public static final int x=bar();
    static boolean foo() {
	return true;
    }
    static int bar() { return 3; }
    public static void main(String[] args) {
    }

}

aspect FIAspect {
    before() : set(int FieldInit.x) { System.out.println("foo"); }
}
	
